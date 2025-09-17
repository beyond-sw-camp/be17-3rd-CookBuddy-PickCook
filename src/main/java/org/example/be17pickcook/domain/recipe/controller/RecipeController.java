package org.example.be17pickcook.domain.recipe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.recipe.model.RecipeListResponseDto;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.recipe.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 레시피 관리 컨트롤러
 * - 레시피 CRUD API
 * - 레시피 목록 조회 (페이징, 정렬)
 * - 레시피 추천 기능
 * - 이미지 첨부 지원
 */
@Tag(name = "레시피 관리", description = "레시피 등록, 조회, 목록 조회 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe")
public class RecipeController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final RecipeService recipeService;

    // =================================================================
    // 레시피 등록 관련 API
    // =================================================================

    @Operation(
            summary = "레시피 등록",
            description = "사용자가 새로운 레시피를 등록합니다. " +
                    "레시피 정보는 RecipeRequestDto로 전달하고, " +
                    "이미지 파일은 optional로 MultipartFile 리스트 형태로 전달 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "레시피 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping(value="/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> register(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "레시피 등록 정보", required = true)
            @RequestPart RecipeDto.RecipeRequestDto dto,
            @Parameter(description = "레시피 이미지 파일들 (선택사항)")
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws SQLException, IOException {

        recipeService.register(authUser, dto, files);
        return ResponseEntity.ok(BaseResponse.success("레시피 작성 성공"));
    }

    // =================================================================
    // 레시피 조회 관련 API
    // =================================================================

    @Operation(
            summary = "레시피 목록 조회 (페이징)",
            description = "등록된 레시피 목록을 페이지 단위로 조회합니다. " +
                    "page: 0부터 시작하는 페이지 번호, " +
                    "size: 페이지당 레코드 수, " +
                    "sortType: 정렬 방식 (latest: 최신순, oldest: 오래된순, likes: 좋아요순, scraps: 스크랩순)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 파라미터")
            }
    )
    @GetMapping
    public BaseResponse<PageResponse<RecipeDto.RecipeListResponseDto>> getRecipeList(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 레시피 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방식 (latest, oldest)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortType,
            @Parameter(description = "난이도 필터 (쉬움, 보통, 어려움)")
            @RequestParam(required = false) String difficulty,
            @Parameter(description = "카테고리 필터 (반찬, 국&찌개, 일품, 밥, 후식, 기타)")
            @RequestParam(required = false) String category,
            @Parameter(description = "조리방법 필터 (끓이기, 굽기, 볶기, 찌기, 튀기기, 기타)")
            @RequestParam(required = false) String cookingMethod) {
            @Parameter(description = "정렬 방식 (latest, oldest, likes, scraps)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortType) {


        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;

        Sort sort = switch (sortType) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "scraps" -> Sort.by(Sort.Direction.DESC, "scrapCount");

            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // latest
        };

        Pageable pageable = PageRequest.of(page, size, sort);


        // 필터링된 레시피 목록 조회
        return BaseResponse.success(
                recipeService.getFilteredRecipeList(userIdx, pageable, difficulty, category, cookingMethod)
        );

    }

    @Operation(
            summary = "특정 레시피 조회",
            description = "레시피 ID를 기반으로 레시피 상세 정보를 조회합니다. " +
                    "조회수가 증가하고 사용자별 좋아요/스크랩 상태도 함께 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없음")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto.RecipeResponseDto> getRecipe(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "조회할 레시피 ID", example = "1")
            @PathVariable Long id) {

        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;
        return ResponseEntity.ok(recipeService.getRecipe(id, userIdx));
    }

    // =================================================================
    // 레시피 추천 관련 API
    // =================================================================

    @Operation(
            summary = "사용자 맞춤 레시피 추천",
            description = "사용자의 냉장고 식재료와 선호도를 기반으로 맞춤 레시피를 추천합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/recommendation")
    public BaseResponse<PageResponse<RecipeListResponseDto>> getRecommendations(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam int size) {

        return BaseResponse.success(recipeService.getRecommendations(authUser.getIdx(), page, size));
    }



    @Operation(
            summary = "레시피 검색 (페이징 + 정렬 + 검색)",
            description = "사용자가 입력한 키워드로 레시피를 검색한 결과를 제공합니다."
    )
    @GetMapping("/search")
    public BaseResponse<PageResponse<RecipeDto.RecipeListResponseDto>> getRecipeKeyword(
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "검색 키워드", example = "스파게티")
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 게시글 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방향 (DESC: 최신순, ASC: 오래된순)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String dir) {

        Integer userIdx = (authUser != null) ? authUser.getIdx() : null;

        Page<RecipeDto.RecipeListResponseDto> recipes = recipeService.getRecipeKeyword(keyword, page, size, dir, userIdx);
        return BaseResponse.success(PageResponse.from(recipes));
    }
}