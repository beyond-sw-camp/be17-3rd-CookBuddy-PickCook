package org.example.be17pickcook.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeService likesService;
    private final ScrapService scrapService;
    private final CommentService commentService;

    // 전체 게시글 조회
    public List<PostDto.ListResponse> getAllPosts() {
        List<Post> postList = postRepository.findAll();
        return postList.stream()
                .map(post -> {
                    int likesCount = likesService.getLikeCount(LikeTargetType.POST, post.getId());
                    int scrapsCount = scrapService.getScrapCount(ScrapTargetType.POST, post.getId());
                    int commentCount = commentService.getCommentsCountByPost(post.getId());
                    return PostDto.ListResponse.from(post, likesCount, scrapsCount, commentCount);
                })
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    public PostDto.DetailResponse getPostById(int userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        int likesCount = likesService.getLikeCount(LikeTargetType.POST, post.getId());
        boolean hasLiked = likesService.hasUserLiked(userId, LikeTargetType.POST, postId);
        int scrapsCount = scrapService.getScrapCount(ScrapTargetType.POST, post.getId());
        boolean hasScrapped = likesService.hasUserLiked(userId, LikeTargetType.POST, postId);

        return PostDto.DetailResponse.from(post, likesCount, hasLiked, scrapsCount, hasScrapped);
    }

    // 게시글 작성
    public PostDto.DetailResponse createPost(PostDto.Request dto, UserDto.AuthUser authUser) {
        // DB에서 User 조회
        User user = userRepository.findById(authUser.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Post 생성
        Post post = dto.toEntity(user); // toEntity에서 User 객체 받도록 수정

        Post saved = postRepository.save(post);
        int likesCount = likesService.getLikeCount(LikeTargetType.POST, post.getId());
        int scrapsCount = scrapService.getScrapCount(ScrapTargetType.POST, post.getId());
        return PostDto.DetailResponse.from(saved, likesCount, false, scrapsCount, false);
    }

}

