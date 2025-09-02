package org.example.be17pickcook.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.model.PostImage;
import org.example.be17pickcook.domain.community.repository.PostImageRepository;
import org.example.be17pickcook.domain.community.repository.PostQueryRepository;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostQueryRepository postQueryRepository;
    private final UserRepository userRepository;
    private final LikeService likesService;
    private final ScrapService scrapService;
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
    public PostDto.DetailResponse getPostById(int userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        boolean hasLiked = likesService.hasUserLiked(userId, LikeTargetType.POST, postId);
        boolean hasScrapped = scrapService.hasUserScrapped(userId, ScrapTargetType.POST, postId);

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

}

