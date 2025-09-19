package org.example.be17pickcook.domain.recipe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.common.service.S3UploadService;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.repository.LikeRepository;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.recipe.model.*;
import org.example.be17pickcook.domain.recipe.repository.RecipeIngredientRepository;
import org.example.be17pickcook.domain.recipe.repository.RecipeQueryRepository;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;
import org.example.be17pickcook.domain.refrigerator.repository.RefrigeratorItemRepository;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.repository.ScrapRepository;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final S3UploadService s3UploadService;
    private final LikeService likesService;
    private final ScrapService scrapService;
    private final LikeRepository likesRepository;
    private final ScrapRepository scrapRepository;
    private final RecipeQueryRepository recipeQueryRepository;


    // 레시피 등록
    @Transactional
    public void register(UserDto.AuthUser authUser,
                         RecipeDto.RecipeRequestDto dto,
                         List<MultipartFile> files) throws SQLException, IOException {

        // 만약 이미지 없을 경우
        if (files == null) {
            files = List.of(); // 빈 리스트로 초기화
        }

        // 대표 이미지 업로드 (첫 2장은 대표 이미지 small, large)
        String imageSmallUrl = (files.size() > 0 && !files.get(0).isEmpty()) ?
                s3UploadService.upload(files.get(0)) : null;

        String imageLargeUrl = (files.size() > 1 && !files.get(1).isEmpty()) ?
                s3UploadService.upload(files.get(1)) : null;


        // 기본 Recipe 엔티티 생성
        Recipe recipe = dto.toEntity(User.builder().idx(authUser.getIdx()).build());

        // 대표 이미지 적용
        recipe.setImage_small_url(imageSmallUrl);
        recipe.setImage_large_url(imageLargeUrl);

        // Steps 매핑 및 이미지 업로드
        if (dto.getSteps() != null) {
            for (int i = 0; i < dto.getSteps().size(); i++) {
                RecipeDto.RecipeStepDto stepDto = dto.getSteps().get(i);
                String stepImageUrl = (files.size() > i + 2 && !files.get(i + 2).isEmpty()) ?
                        s3UploadService.upload(files.get(i + 2)) : null;
                RecipeStep step = stepDto.toEntity(recipe, i + 1);
                step.setImage_url(stepImageUrl);

                recipe.addSteps(step);
            }
        }

        recipeRepository.save(recipe);
    }



    // 특정 레시피 조회 + 좋아요 정보 + 스크랩 정보 포함
    public RecipeDto.RecipeResponseDto getRecipe(Long recipeId, Integer userIdx) {
        Recipe recipe = recipeRepository.findDetailById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다. id=" + recipeId));

//        Integer likeCount = likesService.getLikeCount(LikeTargetType.RECIPE, recipeId);
        Boolean likedByUser = userIdx != null &&
                likesService.hasUserLiked(userIdx, LikeTargetType.RECIPE, recipeId);

//        Integer scrapCount = scrapService.getScrapCount(ScrapTargetType.RECIPE, recipeId);
        Boolean scrapedByUser = userIdx != null &&
                scrapService.hasUserScrapped(userIdx, ScrapTargetType.RECIPE, recipeId);

        RecipeDto.RecipeResponseDto dto = RecipeDto.RecipeResponseDto.fromEntity(recipe);
        dto.setLikeInfo(likedByUser);
        dto.setScrapInfo(scrapedByUser);

        return dto;
    }



    public PageResponse<RecipeDto.RecipeListResponseDto> getRecipeListWithFilter(
            Integer userIdx, int page, int size, String sortType,
            String difficulty, String category, String cookingMethod) {

        // 1. RecipeQueryRepository를 통해 필터링된 레시피 조회
        Page<RecipeDto.RecipeListResponseDto> recipePage = recipeQueryRepository.getRecipesWithFilter(
                page, size, sortType, difficulty, category, cookingMethod, userIdx);

        // 2. recipeIds 추출 (좋아요/스크랩 정보 조회용)
        List<Long> recipeIds = recipePage.getContent().stream()
                .map(RecipeDto.RecipeListResponseDto::getIdx)
                .toList();

        if (recipeIds.isEmpty()) {
            return PageResponse.from(recipePage);
        }

        // 3. 로그인 사용자 기준 좋아요 여부 조회
        Set<Long> likedByUser = (userIdx == null) ? Collections.emptySet() :
                new HashSet<>(likesRepository.findLikedRecipeIdsByUser(
                        LikeTargetType.RECIPE, userIdx, recipeIds));

        // 4. 로그인 사용자 기준 스크랩 여부 조회
        Set<Long> scrappedByUser = (userIdx == null) ? Collections.emptySet() :
                new HashSet<>(scrapRepository.findScrappedRecipeIdsByUser(
                        ScrapTargetType.RECIPE, userIdx, recipeIds));

        // 5. 좋아요/스크랩 정보를 DTO에 설정
        recipePage.getContent().forEach(dto -> {
            dto.setLikedByUser(likedByUser.contains(dto.getIdx()));
            dto.setScrapInfo(scrappedByUser.contains(dto.getIdx()));
        });

        return PageResponse.from(recipePage);
    }

    public PageResponse<RecipeListResponseDto> getRecommendations(Integer userIdx, int page, int size) {
        // 1. 사용자 냉장고 재료 조회
        Set<String> userItemNames = refrigeratorItemRepository.findUsableItems(userIdx, LocalDate.now()).stream()
                .map(item -> item.getIngredientName().toLowerCase())
                .collect(Collectors.toSet());

        // 2. 레시피 재료 조회
        List<Object[]> rawIngredients = recipeIngredientRepository.findAllRecipeIngredients();

        Map<Long, List<String>> recipeIngredientMap = rawIngredients.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).longValue(),
                        Collectors.mapping(row -> ((String) row[1]).toLowerCase(), Collectors.toList())
                ));

        // 3. 매칭 개수 계산
        Map<Long, Integer> recipeMatchCount = recipeIngredientMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (int) e.getValue().stream().filter(userItemNames::contains).count()
                ));

        // 4. 추천 레시피 ID 정렬
        List<Long> recommendedRecipeIds = recipeMatchCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        if (recommendedRecipeIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0);
        }

        // 5. DTO 조회
        List<RecipeListResponseDto> dtos = recipeRepository.findAllOnlyRecipeWithIds(recommendedRecipeIds);

        // 6. 추천 순서대로 정렬
        Map<Long, RecipeListResponseDto> dtoMap = dtos.stream()
                .collect(Collectors.toMap(RecipeListResponseDto::getIdx, Function.identity()));

        List<RecipeListResponseDto> scoredRecipes = recommendedRecipeIds.stream()
                .map(dtoMap::get)
                .toList();

        // 7. 페이징
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, scoredRecipes.size());
        List<RecipeListResponseDto> pageContent =
                fromIndex < toIndex ? scoredRecipes.subList(fromIndex, toIndex) : Collections.emptyList();

        int totalElements = scoredRecipes.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResponse<>(pageContent, page, size, totalElements, totalPages);
    }



    // 레시피 검색
    public Page<RecipeDto.RecipeListResponseDto> getRecipeKeyword(String keyword, int page, int size, String dir, Integer userIdx) {
        return recipeQueryRepository.getRecipesFiltered(keyword, page, size, dir, userIdx);
    }
}
