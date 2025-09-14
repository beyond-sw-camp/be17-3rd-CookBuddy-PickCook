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

    // 기본 이미지
    private static final String DEFAULT_SMALL_IMAGE = "https://example.com/default-small.jpg";
    private static final String DEFAULT_LARGE_IMAGE = "https://example.com/default-large.jpg";
    private static final String DEFAULT_STEP_IMAGE  = "https://example.com/default-step.jpg";
    private final UserRepository userRepository;
    private final RecipeQueryRepository recipeQueryRepository;


    // 레시피 등록
    @Transactional
    public void register(UserDto.AuthUser authUser,
                         RecipeDto.RecipeRequestDto dto,
                         List<MultipartFile> files) throws SQLException, IOException {

        // 대표 이미지 업로드 (첫 2장은 대표 이미지 small, large)
        String imageSmallUrl = (files.size() > 0 && !files.get(0).isEmpty()) ?
                s3UploadService.upload(files.get(0)) : DEFAULT_SMALL_IMAGE;

        String imageLargeUrl = (files.size() > 1 && !files.get(1).isEmpty()) ?
                s3UploadService.upload(files.get(1)) : DEFAULT_LARGE_IMAGE;


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
                        s3UploadService.upload(files.get(i + 2)) : DEFAULT_STEP_IMAGE;
                RecipeStep step = stepDto.toEntity(recipe);
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

    // 레시피 전체 목록 조회 + 좋아요 정보 + 스크랩 정보 포함
//    public PageResponse<RecipeDto.RecipeResponseDto> getRecipeList(Integer userIdx, Pageable pageable) {
//        Page<RecipeDto.RecipeResponseDto> recipePage = recipeRepository.findAll(pageable)
//                .map(recipe -> {
//                    Integer likeCount = likesService.getLikeCount(LikeTargetType.RECIPE, recipe.getIdx());
//                    Boolean likedByUser = userIdx != null &&
//                            likesService.hasUserLiked(userIdx, LikeTargetType.RECIPE, recipe.getIdx());
//
//                    Boolean scrapedByUser = userIdx != null &&
//                            scrapService.hasUserScrapped(userIdx, ScrapTargetType.RECIPE, recipe.getIdx());
//
//                    RecipeDto.RecipeResponseDto dto = RecipeDto.RecipeResponseDto.fromEntity(recipe);
//                    dto.setLikeInfo(likeCount, likedByUser);
//                    dto.setScrapInfo(scrapedByUser);
//                    return dto;
//                });
//
//        return PageResponse.from(recipePage);
//    }

    public PageResponse<RecipeDto.RecipeListResponseDto> getRecipeList(Integer userIdx, Pageable pageable) {
        // 1. 레시피 페이징 조회 (부분 컬럼만 Object[]로)
        Page<Object[]> recipePage = recipeRepository.findAllOnlyRecipe(pageable);

        // 2. DTO 변환 및 recipeIds 추출
        List<Long> recipeIds = new ArrayList<>();
        Page<RecipeDto.RecipeListResponseDto> dtoPage = recipePage.map(arr -> {
            Long idx = (Long) arr[0];
            recipeIds.add(idx); // 좋아요/스크랩 조회용

            return RecipeDto.RecipeListResponseDto.builder()
                    .idx(idx)
                    .title((String) arr[1])
                    .cooking_method((String) arr[2])
                    .category((String) arr[3])
                    .time_taken((String) arr[4])
                    .difficulty_level((String) arr[5])
                    .serving_size((String) arr[6])
                    .hashtags((String) arr[7])
                    .image_large_url((String) arr[8])
                    .likeCount((Long) arr[9])
                    .scrapCount((Long) arr[10])
                    .description((String) arr[11])
                    .build();
        });

        // 3. 좋아요 개수 한 번에 조회
//        Map<Long, Long> likeCounts = recipeIds.isEmpty() ? Collections.emptyMap() :
//                likesRepository.countLikesByRecipeIds(LikeTargetType.RECIPE, recipeIds)
//                        .stream()
//                        .collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (Long) arr[1]));

        // 4. 로그인 사용자 기준 좋아요 여부
        Set<Long> likedByUser = (userIdx == null || recipeIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(likesRepository.findLikedRecipeIdsByUser(LikeTargetType.RECIPE, userIdx, recipeIds));

        // 5. 로그인 사용자 기준 스크랩 여부
        Set<Long> scrappedByUser = (userIdx == null || recipeIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(scrapRepository.findScrappedRecipeIdsByUser(ScrapTargetType.RECIPE, userIdx, recipeIds));

        // 6. 좋아요/스크랩 정보 DTO에 세팅
        dtoPage.forEach(dto -> {
            dto.setLikedByUser(
//                    likeCounts.getOrDefault(dto.getIdx(), 0L).intValue(),
                    likedByUser.contains(dto.getIdx()));
            dto.setScrapInfo(scrappedByUser.contains(dto.getIdx()));
        });

        return PageResponse.from(dtoPage);
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
    public Page<RecipeDto.RecipeListResponseDto> getRecipeKeyword(String keyword, int page, int size, String dir) {
        return recipeQueryRepository.getRecipesFiltered(keyword, page, size, dir);
    }
}
