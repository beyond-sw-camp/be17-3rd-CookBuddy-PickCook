package org.example.be17pickcook.domain.recipe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.common.service.S3UploadService;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.repository.LikeRepository;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.recipe.model.RecipeStep;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.repository.ScrapRepository;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final S3UploadService s3UploadService;
    private final LikeService likesService;
    private final ScrapService scrapService;
    private final LikeRepository likesRepository;
    private final ScrapRepository scrapRepository;

    // 기본 이미지
    private static final String DEFAULT_SMALL_IMAGE = "https://example.com/default-small.jpg";
    private static final String DEFAULT_LARGE_IMAGE = "https://example.com/default-large.jpg";
    private static final String DEFAULT_STEP_IMAGE  = "https://example.com/default-step.jpg";


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
            }
        }

        recipeRepository.save(recipe);
    }



    // 특정 레시피 조회 + 좋아요 정보 + 스크랩 정보 포함
    public RecipeDto.RecipeResponseDto getRecipe(Long recipeId, Integer userIdx) {
        Recipe recipe = recipeRepository.findDetailById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다. id=" + recipeId));

        Integer likeCount = likesService.getLikeCount(LikeTargetType.RECIPE, recipeId);
        Boolean likedByUser = userIdx != null &&
                likesService.hasUserLiked(userIdx, LikeTargetType.RECIPE, recipeId);

        Integer scrapCount = scrapService.getScrapCount(ScrapTargetType.RECIPE, recipeId);
        Boolean scrapedByUser = userIdx != null &&
                scrapService.hasUserScrapped(userIdx, ScrapTargetType.RECIPE, recipeId);

        RecipeDto.RecipeResponseDto dto = RecipeDto.RecipeResponseDto.fromEntity(recipe);
        dto.setLikeInfo(likeCount, likedByUser);
        dto.setScrapInfo(scrapCount, scrapedByUser);

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


}
