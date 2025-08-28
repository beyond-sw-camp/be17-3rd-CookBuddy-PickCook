package org.example.be17pickcook.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.repository.PostQueryRepository;
import org.example.be17pickcook.domain.community.service.PostService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final PostQueryRepository postQueryRepository;

    @Operation(summary = "게시글 목록 조회", description = "전체 게시글 목록을 조회합니다.")
    @GetMapping
    public BaseResponse<List<PostDto.ListResponse>> getAllPosts() {
        List<PostDto.ListResponse> posts = postService.getAllPosts();
        return BaseResponse.success(posts);
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public BaseResponse<PostDto.DetailResponse> getPost(@AuthenticationPrincipal UserDto.AuthUser authUser, @PathVariable @Schema(description = "조회할 게시글 ID", example = "1") Long id) {
        try {
            PostDto.DetailResponse post = postService.getPostById(authUser.getIdx(), id);
            return BaseResponse.success(post);
        } catch (RuntimeException e) {
            return BaseResponse.error(BaseResponseStatus.POST_NOT_FOUND);
        }
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @PostMapping
    public BaseResponse<PostDto.DetailResponse> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 생성 DTO",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostDto.Request.class)
                    )
            )
            @RequestBody PostDto.Request postDto,
            @AuthenticationPrincipal UserDto.AuthUser authUser) {
        PostDto.DetailResponse saved = postService.createPost(postDto, authUser);
        return BaseResponse.success(saved);
    }


    @Operation(summary = "게시글 목록 조회(페이징 + 정렬 + 검색",  description = "정렬과 검색이 적용된 게시글 목록 페이지를 조회합니다.")
    @GetMapping("/list")
    public BaseResponse<PageResponse<PostDto.ListResponse>> getPostsWithPaging(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String dir // 최신순 기본
    ) {
        Page<PostDto.ListResponse> posts = postService.getPostsWithPaging(keyword, page, size, dir);
        return BaseResponse.success(PageResponse.from(posts));
    }
}
