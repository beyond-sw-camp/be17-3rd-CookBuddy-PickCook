package org.example.be17pickcook.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.recipe.model.RecipeDto;
import org.example.be17pickcook.domain.recipe.model.RecipeIngredient;
import org.example.be17pickcook.domain.recipe.model.RecipeStep;
import org.example.be17pickcook.domain.recipe.service.S3UploadService;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용
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

        // 대표 이미지 업로드 (첫 2장은 대표 이미지 small, large)
        String main_image_url = (files.size() > 0 && !files.get(0).isEmpty()) ?
                s3UploadService.upload(files.get(0)) : MAIN_IMAGE_URL;

        String detail_image_url = (files.size() > 1 && !files.get(1).isEmpty()) ?
                s3UploadService.upload(files.get(1)) : DETAIL_IMAGE_URL;


        // 기본 Recipe 엔티티 생성
        Product product = dto.toEntity(User.builder().idx(authUser.getIdx()).build());

        // 대표 이미지 적용
        product.setMainImageUrl(main_image_url);
        product.setDetailImageUrl(detail_image_url);

        productRepository.save(product);
    }

    // 전체 조회 (읽기)
    public List<ProductDto.Res> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductDto.Res::from)
                .toList();
    }

    // 단건 조회 (읽기)
    public ProductDto.Res findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));
        return ProductDto.Res.from(product);
    }

    // 수정 (쓰기) - DTO가 엔티티에 값 반영
    @Transactional
    public ProductDto.Res update(Long id, ProductDto.Update dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));

        dto.apply(product);
        return ProductDto.Res.from(product);
    }

    // 가격만 변경 (쓰기) → 편의 메서드 실제 사용
    @Transactional
    public void changePrice(Long id, Integer price) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));
        product.changePrice(price); // 편의 메서드 사용 → 회색표시 사라짐
    }

    // 할인율만 변경 (쓰기) → 편의 메서드 실제 사용
    @Transactional
    public void changeDiscountRate(Long id, Integer rate) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: id=" + id));
        product.changeDiscountRate(rate); // 편의 메서드 사용
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