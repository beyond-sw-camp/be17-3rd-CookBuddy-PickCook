package org.example.be17pickcook.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "게시글 목록 조회", description = "전체 게시글 목록을 조회합니다.")
    @GetMapping
    public BaseResponse<List<PostDto.Response>> getAllPosts() {
        List<PostDto.Response> posts = postService.getAllPosts();
        return BaseResponse.success(posts);
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public BaseResponse<PostDto.Response> getPost(@PathVariable @Schema(description = "조회할 게시글 ID", example = "1") Long id) {
        try {
            PostDto.Response post = postService.getPostById(id);
            return BaseResponse.success(post);
        } catch (RuntimeException e) {
            return BaseResponse.error(BaseResponseStatus.POST_NOT_FOUND);
        }
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @PostMapping
    public BaseResponse<PostDto.Response> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 생성 DTO",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostDto.Request.class)
                    )
            )
            @RequestBody PostDto.Request postDto,
            @AuthenticationPrincipal UserDto.AuthUser authUser) {
        PostDto.Response saved = postService.createPost(postDto, authUser);
        return BaseResponse.success(saved);
    }
}
