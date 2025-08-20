package org.example.be17pickcook.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.recipe.service.S3UploadService;
import org.springframework.data.domain.Page;                          // [변경] 페이징
import org.springframework.data.domain.Pageable;                    // [변경] 페이징
import org.springframework.data.domain.Sort;                        // [변경] 정렬
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final S3UploadService s3UploadService;

    private static final String MAIN_IMAGE_URL = "https://example.com/default-small.jpg";
    private static final String DETAIL_IMAGE_URL = "https://example.com/default-large.jpg";

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

    // 전체 조회 (페이징 + 정렬)  // [변경] 시그니처 교체
    public Page<ProductDto.Res> findAll(Pageable pageable) {            // [변경]
        return productRepository.findAll(pageable).map(ProductDto.Res::from); // [변경]
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
