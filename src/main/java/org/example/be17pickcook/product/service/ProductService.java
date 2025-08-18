package org.example.be17pickcook.product.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.product.repository.ProductRepository;
import org.example.be17pickcook.product.model.Product;
import org.example.be17pickcook.product.model.ProductDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용
public class ProductService {

    private final ProductRepository productRepository;

    // 등록 (쓰기)
    @Transactional
    public ProductDto.Res register(ProductDto.Register dto) {
        Product product = dto.toEntity();
        Product saved = productRepository.save(product);
        return ProductDto.Res.from(saved);
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
