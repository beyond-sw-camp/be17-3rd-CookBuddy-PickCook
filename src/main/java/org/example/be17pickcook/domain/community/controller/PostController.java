package org.example.be17pickcook.domain.community.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.service.PostService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public BaseResponse<List<PostDto.Response>> getAllPosts() {
        List<PostDto.Response> posts = postService.getAllPosts();
        return BaseResponse.success(posts);
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public BaseResponse<PostDto.Response> getPost(@PathVariable Long id) {
        try {
            PostDto.Response post = postService.getPostById(id);
            return BaseResponse.success(post);
        } catch (RuntimeException e) {
            return BaseResponse.error(BaseResponseStatus.POST_NOT_FOUND);
        }
    }

    // 게시글 작성
    @PostMapping
    public BaseResponse<PostDto.Response> createPost(@RequestBody PostDto.Request postDto, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        PostDto.Response saved = postService.createPost(postDto, authUser);
        return BaseResponse.success(saved);
    }
}
