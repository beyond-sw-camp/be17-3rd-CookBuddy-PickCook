package org.example.be17pickcook.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.repository.PostQueryRepository;
import org.example.be17pickcook.domain.community.service.PostService;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 커뮤니티 게시글 컨트롤러
 * - 게시글 CRUD API
 * - 페이징 및 정렬 기능
 * - 검색 및 필터링 지원
 * - 좋아요/스크랩 기능 연동
 */
@Tag(name = "커뮤니티 게시글 관리", description = "게시글 작성, 조회, 목록 조회 및 검색 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final PostService postService;
    private final PostQueryRepository postQueryRepository;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    @Operation(
            summary = "게시글 목록 조회",
            description = "전체 게시글 목록을 조회합니다. 기본적인 목록 조회 기능입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping
    public BaseResponse<List<PostDto.ListResponse>> getAllPosts() {
        List<PostDto.ListResponse> posts = postService.getAllPosts();
        return BaseResponse.success(posts);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 게시글 상세 정보를 조회합니다. 조회수가 증가하고 사용자별 좋아요/스크랩 상태도 함께 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/{id}")
    public BaseResponse<PostDto.DetailResponse> getPost(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable Long id) {
        try {
            PostDto.DetailResponse post = postService.getPostById(authUser.getIdx(), id);
            return BaseResponse.success(post);
        } catch (RuntimeException e) {
            return BaseResponse.error(BaseResponseStatus.POST_NOT_FOUND);
        }
    }

    @Operation(
            summary = "게시글 작성",
            description = "새 게시글을 작성합니다. 로그인한 사용자만 작성 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "작성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @PostMapping
    public BaseResponse<String> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 생성 DTO",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = PostDto.Request.class)
                    )
            )
            @RequestBody PostDto.Request postDto,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        postService.createPost(postDto, authUser);
        return BaseResponse.success("게시글 등록 성공");
    }

    // =================================================================
    // 검색 및 페이징 관련 API
    // =================================================================

    @Operation(
            summary = "게시글 목록 조회 (페이징 + 정렬 + 검색)",
            description = "정렬과 검색이 적용된 게시글 목록 페이지를 조회합니다. 키워드 검색 및 다양한 정렬 옵션을 지원합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 파라미터")
            }
    )
    @GetMapping("/list")
    public BaseResponse<PageResponse<PostDto.ListResponse>> getPostsWithPaging(
            @Parameter(description = "검색 키워드", example = "레시피")
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 게시글 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방향 (DESC: 최신순, ASC: 오래된순)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String dir) {

        Page<PostDto.ListResponse> posts = postService.getPostsWithPaging(keyword, page, size, dir);
        return BaseResponse.success(PageResponse.from(posts));
    }

    @Operation(
            summary = "메인페이지용 게시글 목록 조회",
            description = "메인페이지에 표시할 게시글 목록을 조회합니다. 다양한 정렬 옵션(최신순, 좋아요순, 스크랩순 등)을 지원합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 정렬 옵션")
            }
    )
    @GetMapping("/mplist")
    public BaseResponse<PageResponse<PostDto.PostCardResponse>> getMainPosts(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 게시글 수", example = "4")
            @RequestParam(defaultValue = "4") int size,
            @Parameter(description = "정렬 타입 (latest: 최신순, oldest: 오래된순, likes: 좋아요순, scraps: 스크랩순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortType) {

        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;

        Sort sort = switch (sortType) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "scraps" -> Sort.by(Sort.Direction.DESC, "scrapCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // latest
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        return BaseResponse.success(postService.getMainPosts(userIdx, pageable));
    }
}