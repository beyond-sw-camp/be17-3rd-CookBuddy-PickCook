package org.example.be17pickcook.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.service.LikesService;
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
    private final LikesService likesService;
    private final ScrapService scrapService;

    // 전체 게시글 조회
    public List<PostDto.Response> getAllPosts() {
        List<Post> postList = postRepository.findAll();
        return postList.stream()
                .map(post -> {
                    int likesCount = likesService.getLikesCount(LikeTargetType.COMMUNITY, post.getId());
                    int scrapsCount = scrapService.getScrapCount(ScrapTargetType.COMMUNITY, post.getId());
                    return PostDto.Response.toResponse(post, likesCount, scrapsCount);
                })
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    public PostDto.Response getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        int likesCount = likesService.getLikesCount(LikeTargetType.COMMUNITY, post.getId());
        int scrapsCount = scrapService.getScrapCount(ScrapTargetType.COMMUNITY, post.getId());
        return PostDto.Response.toResponse(post, likesCount, scrapsCount);
    }

    // 게시글 작성
    public PostDto.Response createPost(PostDto.Request dto, UserDto.AuthUser authUser) {
        // DB에서 User 조회
        User user = userRepository.findById(authUser.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Post 생성
        Post post = dto.toEntity(user); // toEntity에서 User 객체 받도록 수정

        Post saved = postRepository.save(post);
        int likesCount = likesService.getLikesCount(LikeTargetType.COMMUNITY, post.getId());
        int scrapsCount = scrapService.getScrapCount(ScrapTargetType.COMMUNITY, post.getId());
        return PostDto.Response.toResponse(saved, likesCount, scrapsCount);
    }

}

