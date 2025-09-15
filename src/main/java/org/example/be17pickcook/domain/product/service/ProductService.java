package org.example.be17pickcook.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.domain.cart.repository.CartsRepository;
import org.example.be17pickcook.domain.cart.service.CartsService;
import org.example.be17pickcook.domain.common.model.Category;
import org.example.be17pickcook.domain.common.repository.CategoryRepository;
import org.example.be17pickcook.domain.product.mapper.ProductMapper;
import org.example.be17pickcook.domain.product.repository.ProductQueryRepository;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.review.model.Review;
import org.example.be17pickcook.domain.review.model.ReviewDto;
import org.example.be17pickcook.domain.review.repository.ReviewRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.common.service.S3UploadService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final S3UploadService s3UploadService;
    private final ReviewRepository reviewRepository;

    private static final String MAIN_IMAGE_URL = "https://example.com/default-small.jpg";
    private static final String DETAIL_IMAGE_URL = "https://example.com/default-large.jpg";
    private final CartsRepository cartsRepository;
    private final CategoryRepository categoryRepository;
    private final ProductQueryRepository productQueryRepository;

    // 등록 (쓰기)
    @Transactional
    public void register(UserDto.AuthUser authUser,
                         ProductDto.Register dto,
                         List<MultipartFile> files) throws SQLException, IOException {

        // [변경] files null/빈값 안전 처리
        List<MultipartFile> safe = (files == null) ? List.of() : files;  // [변경]

        // 대표 이미지 업로드 (첫 2장: main, detail)
        String main_image_url = (safe.size() > 0 && !safe.get(0).isEmpty())
                ? s3UploadService.upload(safe.get(0)) : MAIN_IMAGE_URL;  // [변경]

        String detail_image_url = (safe.size() > 1 && !safe.get(1).isEmpty())
                ? s3UploadService.upload(safe.get(1)) : DETAIL_IMAGE_URL; // [변경]

        Product product = dto.toEntity(User.builder().idx(authUser.getIdx()).build());

        product.setMainImageUrl(main_image_url);
        product.setDetailImageUrl(detail_image_url);

        productRepository.save(product);
    }

    // =================================================================
    // 리뷰 포함 상품 상세 조회 (추가 필요)
    // =================================================================

    @Transactional(readOnly = true)
    public ProductDto.DetailWithReview getProductDetailWithReview(Long productId, Integer currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + productId));

        // 리뷰 섹션 구성
        ProductDto.ReviewSection reviewSection = buildReviewSection(productId, currentUserId);

        return ProductDto.DetailWithReview.fromEntity(product, reviewSection);
    }

    private ProductDto.ReviewSection buildReviewSection(Long productId, Integer currentUserId) {
        // 리뷰 통계
        var statistics = reviewRepository.getReviewStatistics(productId);

        // 최근 리뷰 10개
        List<Review> recentReviews = reviewRepository.findByComplexFilter(
                productId, null, null, null, "latest", currentUserId);

        // 내 리뷰 찾기
        ReviewDto.Response myReview = null;
        if (currentUserId != null) {
            Optional<Review> myReviewEntity = reviewRepository.findByProductIdAndUserIdxAndIsDeletedFalse(productId, currentUserId);
            if (myReviewEntity.isPresent()) {
                myReview = ReviewDto.Response.fromEntity(myReviewEntity.get(), currentUserId);
            }
        }

        return ProductDto.ReviewSection.builder()
                .statistics(ReviewDto.StatisticsResponse.fromRepositoryResult(statistics))
                .recentReviews(recentReviews.stream()
                        .limit(10)
                        .map(review -> ReviewDto.Response.fromEntity(review, currentUserId))
                        .toList())
                .myReview(myReview)
                .build();
    }

    public Page<ProductDto.Response> getPagedProductsWithReviewsDto(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        Page<Product> productPage = productRepository.findAll(pageable);

        // DTO 변환
        return productPage.map(ProductDto.Response::fromEntity);
    }

    // 전체 조회 (페이징 + 정렬)  // [변경] 시그니처 교체
//    public PageResponse<ProductDto.Res> findAll(Pageable pageable) {            // [변경]
//        return productRepository.findAll(pageable).map(ProductDto.Res::from); // [변경]
//    }


    /**
     * 전체 상품 목록 조회 (실시간 리뷰 통계 포함)
     */
    public PageResponse<ProductDto.ProductListResponse> getProductList(Integer userIdx, Pageable pageable) {
        // 실시간 리뷰 통계가 포함된 상품 목록 조회
        Page<Object[]> productPage = productRepository.findAllProductListWithReviewStats(pageable);

        List<Long> productIds = new ArrayList<>();
        Page<ProductDto.ProductListResponse> dtoPage = productPage.map(arr -> {
            Long id = (Long) arr[0];
            productIds.add(id); // 장바구니 조회용

            return ProductDto.ProductListResponse.builder()
                    .id(id)
                    .title((String) arr[1])
                    .main_image_url((String) arr[2])
                    .discount_rate((Integer) arr[3])
                    .original_price((Integer) arr[4])
                    .review_count((Long) arr[5])        // 실시간 리뷰 개수
                    .average_rating((Double) arr[6])    // 실시간 평균 별점
                    .build();
        });

        // 로그인 사용자 기준 장바구니를 담았는지 여부
        Set<Long> isInCart = (userIdx == null || productIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(cartsRepository.findCartsProductIdsByUser(userIdx, productIds));

        dtoPage.forEach(dto -> {
            dto.setIsInCart(isInCart.contains(dto.getId()));
        });

        log.info("상품 목록 조회 완료 - 총 {}개 상품", dtoPage.getTotalElements());

        return PageResponse.from(dtoPage);
    }

    // 필요 시: 전체 다 가져오기(비권장)  // [변경] 선택 메서드
    public List<ProductDto.Res> findAllNoPaging(Sort sort) {            // [변경]
        return productRepository.findAll(sort).stream()
                .map(ProductDto.Res::from)
                .toList();
    }

    // 단건 조회 (읽기)
    public ProductDto.Res findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));
        return ProductDto.Res.from(product);
    }

    // 수정 (쓰기)
    @Transactional
    public ProductDto.Res update(Long id, ProductDto.Update dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));
        dto.apply(product);
        return ProductDto.Res.from(product);
    }

    // 할인율만 변경 (쓰기)
    @Transactional
    public void changeDiscountRate(Long id, Integer rate) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));
        product.changeDiscountRate(rate);
    }

    // 삭제 (쓰기)
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * 레시피 기반 연관 상품 조회
     */
    @Transactional(readOnly = true)
    public List<ProductDto.RelatedProductResponse> getRelatedProductsByRecipe(Long recipeId) {
        validateRecipeExists(recipeId);

        // 1차: 재료 기반 매칭 상품 조회
        List<ProductDto.RelatedProductResponse> matchedProducts =
                productRepository.findProductsByRecipeIngredients(recipeId, 16);

        log.debug("레시피 연관 상품 매칭 완료: 레시피ID = {}, 매칭 수 = {}", recipeId, matchedProducts.size());

        // 16개 미만일 경우 랜덤 상품으로 보충
        if (matchedProducts.size() < 16) {
            int remainingCount = 16 - matchedProducts.size();
            List<ProductDto.RelatedProductResponse> randomProducts =
                    productRepository.findRandomProducts(remainingCount);

            log.debug("랜덤 상품 보충 완료: 추가 수 = {}", randomProducts.size());

            matchedProducts.addAll(randomProducts);
        }

        return matchedProducts.stream()
                .limit(16)
                .collect(Collectors.toList());
    }

    private void validateRecipeExists(Long recipeId) {
        // Recipe 엔티티 존재 확인 로직
        // 현재 Recipe 리포지토리가 없다면 기본 검증만 수행
        if (recipeId == null || recipeId <= 0) {
            throw BaseException.from(BaseResponseStatus.INVALID_RECIPE_ID);
        }
    }

// =================================================================
// 검색 관련 API
// =================================================================

    /**
     * 카테고리별 상품 조회 (실시간 리뷰 통계 포함)
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDto.ProductListResponse> getProductsByCategory(
            Integer userIdx, Long categoryId, Pageable pageable) {

        // 정렬 필드 검증 추가
        validateSortField(pageable.getSort());

        // 1. 카테고리 존재 여부 검증 및 이름 조회
        Category category = findCategoryById(categoryId);
        String categoryName = category.getName();

        log.info("카테고리별 상품 조회 요청 - categoryId: {}, categoryName: {}, sort: {}",
                categoryId, categoryName, pageable.getSort());

        // 2. 카테고리명으로 상품 조회 (실시간 리뷰 통계 포함)
        Page<Object[]> productPage = productRepository.findByCategoryWithReviewStats(categoryName, pageable);

        List<Long> productIds = new ArrayList<>();
        List<ProductDto.ProductListResponse> dtoList = productPage.getContent().stream()
                .map(arr -> {
                    Long id = (Long) arr[0];
                    productIds.add(id);

                    return ProductDto.ProductListResponse.builder()
                            .id(id)
                            .title((String) arr[1])
                            .main_image_url((String) arr[2])
                            .discount_rate((Integer) arr[3])
                            .original_price((Integer) arr[4])
                            .review_count((Long) arr[5])        // 실시간 리뷰 개수
                            .average_rating((Double) arr[6])    // 실시간 평균 별점
                            .build();
                })
                .toList();

        // 3. Page 객체 재구성
        Page<ProductDto.ProductListResponse> dtoPage = new PageImpl<>(dtoList, pageable, productPage.getTotalElements());

        // 4. 사용자별 장바구니 상태 설정 (기존 패턴과 동일)
        if (userIdx != null) {
            Set<Long> isInCart = cartsRepository.findCartsProductIdsByUser(userIdx, productIds)
                    .stream()
                    .collect(Collectors.toSet());

            dtoList.forEach(dto -> dto.setIsInCart(isInCart.contains(dto.getId())));
        }

        log.info("카테고리별 상품 조회 완료 - categoryName: {}, 총 {}개 상품", categoryName, dtoPage.getTotalElements());

        return PageResponse.from(dtoPage);
    }

    // 정렬 필드 검증 메서드 추가
    private void validateSortField(Sort sort) {
        Set<String> allowedFields = Set.of(
                "id", "title", "original_price", "discount_rate",
                "createdAt", "review_count" // 실제 Product 엔티티 필드명으로 수정
        );

        for (Sort.Order order : sort) {
            if (!allowedFields.contains(order.getProperty())) {
                throw BaseException.from(BaseResponseStatus.INVALID_SORT_FIELD);
            }
        }
    }



    // 상품 검색
    public Page<ProductDto.ProductListResponse> getProductKeyword(String keyword, int page, int size, String dir, Integer userIdx) {
        return productQueryRepository.getRecipesFiltered(keyword, page, size, dir, userIdx);
    }

// =================================================================
// 기타 비즈니스 로직 API
// =================================================================

    /**
     * 카테고리 ID로 Category 엔티티 조회
     */
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 카테고리 요청 - categoryId: {}", categoryId);
                    return BaseException.from(BaseResponseStatus.CATEGORY_NOT_FOUND);
                });
    }
}
