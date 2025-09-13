package org.example.be17pickcook.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.model.PostImage;
import org.example.be17pickcook.domain.community.repository.CommentRepository;
import org.example.be17pickcook.domain.community.repository.PostImageRepository;
import org.example.be17pickcook.domain.community.repository.PostQueryRepository;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.repository.LikeRepository;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.repository.ScrapRepository;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostQueryRepository postQueryRepository;
    private final LikeService likesService;
    private final ScrapService scrapService;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;
    private final CommentService commentService;

    // 전체 게시글 조회
    public List<PostDto.ListResponse> getAllPosts() {
        List<Post> postList = postRepository.findAll();
        return postList.stream()
                .map(post -> {
                    int commentCount = commentService.getCommentsCountByPost(post.getId());
                    return PostDto.ListResponse.from(post, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    @Transactional
    public PostDto.DetailResponse getPostById(int userId, Long postId) {
        // 1. 뷰 카운트 증가
        postRepository.incrementViewCount(postId);

        // 2. 상세 조회 (캐시 클리어 후 fresh select)
        Post post = postRepository.findPostWithDetails(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 3. 좋아요/스크랩 여부
        boolean hasLiked = likesService.hasUserLiked(userId, LikeTargetType.POST, postId);
        boolean hasScrapped = scrapService.hasUserScrapped(userId, ScrapTargetType.POST, postId);

        // 4. DTO 변환
        return PostDto.DetailResponse.from(post, hasLiked, hasScrapped);
    }

    // 게시글 작성
    public void createPost(PostDto.Request dto, UserDto.AuthUser authUser) {
        Post post = dto.toEntity(User.builder().idx(authUser.getIdx()).build()); // toEntity에서 User 객체 받도록 수정
        postRepository.save(post);
    }

    public Page<PostDto.ListResponse> getPostsWithPaging(String keyword, int page, int size, String dir) {
        return postQueryRepository.findPostsWithPaging(keyword, page, size, dir);
    }

    // 메인 화면에서 쓸 게시글 조회
    public PageResponse<PostDto.PostCardResponse> getMainPosts(Integer userIdx, Pageable pageable, String filterType) {
        Page<Object[]> postPage;

        switch (filterType) {
            case "my":
                postPage = postRepository.findAllByAuthorId(userIdx, pageable);
                break;
            case "liked":
                postPage = postRepository.findLikedPostsByUser(userIdx, pageable);
                break;
            case "scrapped":
                postPage = postRepository.findScrappedPostsByUser(userIdx, pageable);
                break;
            case "replied":
                postPage = postRepository.findRepliedPostsByUser(userIdx, pageable);
                break;
            default:
                postPage = postRepository.findAllPostData(pageable);
                break;
        }

        List<Long> postIds = postPage.stream()
                .map(obj -> (Long) obj[0])
                .collect(Collectors.toList());

        // 2. 댓글 수 조회 (별도 쿼리)
        Map<Long, Long> commentCountMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            commentRepository.countCommentsByPostIds(postIds)
                    .forEach(obj -> commentCountMap.put((Long) obj[0], (Long) obj[1]));
        }

        // 3. Object[] -> DTO 변환
        List<PostDto.PostCardResponse> content = postPage.stream()
                .map(obj -> {
                    Long id = (Long) obj[0];
                    return PostDto.PostCardResponse.builder()
                            .id(id)
                            .title((String) obj[1])
                            .postImage((String) obj[2])
                            .authorName((String) obj[3])
                            .authorProfileImage((String) obj[4])
                            .likeCount(obj[5] != null ? (Long) obj[5] : 0L)
                            .scrapCount(obj[6] != null ? (Long) obj[6] : 0L)
                            .viewCount(obj[7] != null ? (Long) obj[7] : 0L)
                            .updatedAt((LocalDateTime) obj[8])
                            .content((String) obj[9])
                            .commentCount(commentCountMap.getOrDefault(id, 0L)) // 댓글 수 추가
                            .hasLiked(false)
                            .hasScrapped(false)
                            .build();
                }).collect(Collectors.toList());

        // 4. 좋아요/스크랩 여부 조회
        Set<Long> likedByUser = (userIdx == null || postIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(likeRepository.findLikedRecipeIdsByUser(LikeTargetType.POST, userIdx, postIds));

        Set<Long> scrappedByUser = (userIdx == null || postIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(scrapRepository.findScrappedRecipeIdsByUser(ScrapTargetType.POST, userIdx, postIds));

        content.forEach(dto -> {
            dto.setHasLiked(likedByUser.contains(dto.getId()));
            dto.setHasScrapped(scrappedByUser.contains(dto.getId()));
        });

        // 5. PageImpl로 감싸서 반환
        return PageResponse.from(new PageImpl<>(content, pageable, postPage.getTotalElements()));
    }


    // 게시글 수정
    @Transactional
    public void updatePost(Long postId, PostDto.Request postDto, UserDto.AuthUser authUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(("게시글이 존재하지 않습니다.")));

        if (!post.getUser().getIdx().equals(authUser.getIdx())) {
            throw new AccessDeniedException("본인 게시글만 수정 가능합니다.");
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        postRepository.save(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, UserDto.AuthUser authUser) {
        Post post = postRepository.findByIdAndUserIdx(postId, authUser.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        postRepository.delete(post);
    }

}

