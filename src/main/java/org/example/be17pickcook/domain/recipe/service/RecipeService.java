package org.example.be17pickcook.domain.recipe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.common.service.S3UploadService;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.repository.LikeRepository;
import org.example.be17pickcook.domain.likes.service.LikeService;
import org.example.be17pickcook.domain.recipe.model.*;
import org.example.be17pickcook.domain.recipe.repository.RecipeCommentRepository;
import org.example.be17pickcook.domain.recipe.repository.RecipeIngredientRepository;
import org.example.be17pickcook.domain.recipe.repository.RecipeQueryRepository;
import org.example.be17pickcook.domain.refrigerator.repository.RefrigeratorItemRepository;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.repository.ScrapRepository;
import org.example.be17pickcook.domain.scrap.service.ScrapService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    private final RecipeCommentRepository recipeCommentRepository;


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
        Recipe recipe = recipeRepository.findById(recipeId)  // <- findDetailById를 findById로 변경
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

            return new RecipeDto.RecipeListResponseDto(
                    idx,
                    (String) arr[1],
                    (String) arr[2],
                    (String) arr[3],
                    (String) arr[4],
                    (String) arr[5],
                    (String) arr[6],
                    (String) arr[7],
                    (String) arr[8],
                    (Long) arr[9],
                    (Long) arr[10],
                    (String) arr[11],
                    false, // likedByUser 기본값
                    false  // scrappedByUser 기본값
            );
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
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, null);
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

        return new PageResponse<>(pageContent, page, size, totalElements, totalPages, null);
    }



    // 레시피 목록 조회 (필터링 + 검색)
    public Page<RecipeDto.RecipeListResponseDto> getRecipeListWithFilter(
            Integer userIdx, String keyword, int page, int size, String sortType,
            String difficulty, String category, String cookingMethod) {

        return recipeQueryRepository.getRecipesFiltered(userIdx, keyword, page, size, sortType, difficulty, category, cookingMethod);
    }


    // 마이페이지에서 쓸 레시피 목록 조회
    public PageResponse<RecipeListResponseDto> getRecipeForMypage(Integer userIdx, Pageable pageable, String filterType) {
        Page<RecipeListResponseDto> recipePage;

        switch (filterType) {
            case "liked":
                recipePage = recipeRepository.findLikedRecipesByUser(userIdx, pageable);
                break;
            case "scrapped":
                recipePage = recipeRepository.findScrappedRecipesByUser(userIdx, pageable);
                break;
            case "replied":
                recipePage = recipeRepository.findRepliedRecipesByUser(userIdx, pageable);
                break;
            default:
                recipePage = recipeRepository.findAllByAuthorId(userIdx, pageable);
                break;
        }

        List<Long> recipeIds = recipePage.stream()
                .map(RecipeListResponseDto::getIdx)
                .toList();

        // ✅ 댓글 수 조회
        Map<Long, Long> commentCountMap = new HashMap<>();
        if (!recipeIds.isEmpty()) {
            recipeCommentRepository.countCommentsByRecipeIds(recipeIds)
                    .forEach(obj -> commentCountMap.put((Long) obj[0], (Long) obj[1]));
        }

        // ✅ 좋아요/스크랩 여부 조회
        Set<Long> likedByUser = (userIdx == null || recipeIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(likesRepository.findLikedRecipeIdsByUser(LikeTargetType.RECIPE, userIdx, recipeIds));

        Set<Long> scrappedByUser = (userIdx == null || recipeIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(scrapRepository.findScrappedRecipeIdsByUser(ScrapTargetType.RECIPE, userIdx, recipeIds));

        // ✅ DTO에 값 세팅
        recipePage.forEach(dto -> {
            dto.setLikedByUser(likedByUser.contains(dto.getIdx()));
            dto.setScrapInfo(scrappedByUser.contains(dto.getIdx()));
            dto.setCommentCount(commentCountMap.getOrDefault(dto.getIdx(), 0L));
        });

        return PageResponse.from(recipePage);
    }

    // 레시피 수정
    @Transactional
    public void updateRecipe(Long recipeId, RecipeDto.RecipeRequestDto recipeDto,
                             List<MultipartFile> files, UserDto.AuthUser authUser) throws IOException, SQLException {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피가 존재하지 않습니다."));

        if (!recipe.getUser().getIdx().equals(authUser.getIdx())) {
            throw new AccessDeniedException("본인 레시피만 수정 가능합니다.");
        }

        // 기존 이미지 삭제
        if (files != null && !files.isEmpty()) {
            if (files.size() > 0 && recipe.getImage_small_url() != null) {
                s3UploadService.deleteByUrl(recipe.getImage_small_url());
            }
            if (files.size() > 1 && recipe.getImage_large_url() != null) {
                s3UploadService.deleteByUrl(recipe.getImage_large_url());
            }
        }

        // 새 이미지 업로드
        if (files != null) {
            if (files.size() > 0 && !files.get(0).isEmpty()) {
                recipe.setImage_small_url(s3UploadService.upload(files.get(0)));
            }
            if (files.size() > 1 && !files.get(1).isEmpty()) {
                recipe.setImage_large_url(s3UploadService.upload(files.get(1)));
            }
        }

        // 기본 필드 업데이트
        recipe.setTitle(recipeDto.getTitle());
        recipe.setDescription(recipeDto.getDescription());
        recipe.setCooking_method(recipeDto.getCooking_method());
        recipe.setCategory(recipeDto.getCategory());
        recipe.setTime_taken(recipeDto.getTime_taken());
        recipe.setDifficulty_level(recipeDto.getDifficulty_level());
        recipe.setServing_size(recipeDto.getServing_size());
        recipe.setHashtags(recipeDto.getHashtags());
        recipe.setTip(recipeDto.getTip());

        // Steps, Ingredients 교체
        recipe.clearSteps();       // 기존 Steps 삭제
        recipe.clearIngredients(); // 기존 재료 삭제

        if (recipeDto.getSteps() != null) {
            for (int i = 0; i < recipeDto.getSteps().size(); i++) {
                RecipeDto.RecipeStepDto stepDto = recipeDto.getSteps().get(i);
                String stepImageUrl = (files != null && files.size() > i + 2 && !files.get(i + 2).isEmpty())
                        ? s3UploadService.upload(files.get(i + 2)) : null;
                RecipeStep step = stepDto.toEntity(recipe, i + 1);
                step.setImage_url(stepImageUrl);
                recipe.addSteps(step);
            }
        }

        if (recipeDto.getIngredients() != null) {
            for (RecipeDto.RecipeIngredientDto ingDto : recipeDto.getIngredients()) {
                RecipeIngredient ing = ingDto.toEntity(recipe);
                recipe.addIngredient(ing);
            }
        }

        recipeRepository.save(recipe);
    }




    @Transactional
    public void deleteRecipe(Long recipeId, UserDto.AuthUser authUser) {
        Recipe recipe = recipeRepository.findByIdxAndUserIdx(recipeId, authUser.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 레시피 대표 이미지 삭제
        if (recipe.getImage_small_url() != null) {
            s3UploadService.deleteByUrl(recipe.getImage_small_url());
        }
        if (recipe.getImage_large_url() != null) {
            s3UploadService.deleteByUrl(recipe.getImage_large_url());
        }

        // Steps 이미지 삭제
        if (recipe.getSteps() != null) {
            recipe.getSteps().forEach(step -> {
                if (step.getImage_url() != null) {
                    s3UploadService.deleteByUrl(step.getImage_url());
                }
            });
        }

        // 레시피 삭제
        recipeRepository.delete(recipe);
    }

}
