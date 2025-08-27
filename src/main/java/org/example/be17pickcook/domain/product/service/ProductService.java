package org.example.be17pickcook.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.PageResponse;
import org.example.be17pickcook.domain.cart.repository.CartsRepository;
import org.example.be17pickcook.domain.cart.service.CartsService;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.common.service.S3UploadService;
import org.springframework.data.domain.Page;                          // [변경] 페이징
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;                    // [변경] 페이징
import org.springframework.data.domain.Sort;                        // [변경] 정렬
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final S3UploadService s3UploadService;
    private final CartsService cartsService;

    private static final String MAIN_IMAGE_URL = "https://example.com/default-small.jpg";
    private static final String DETAIL_IMAGE_URL = "https://example.com/default-large.jpg";
    private final CartsRepository cartsRepository;

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


    public PageResponse<ProductDto.ProductListResponse> getProductList(Integer userIdx, Pageable pageable) {            // [변경]
        Page<Object[]> productPage = productRepository.findAllOnlyProductList(pageable);  // [변경]

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
                    .review_count((Long) arr[5])
                    .build();
        });

        // 로그인 사용자 기준 장바구니를 담았는지 여부
        Set<Long> isInCart = (userIdx == null || productIds.isEmpty()) ? Collections.emptySet() :
                new HashSet<>(cartsRepository.findCartsProductIdsByUser(userIdx, productIds));

        dtoPage.forEach(dto -> {
            dto.setIsInCart(isInCart.contains(dto.getId()));
        });

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
}
