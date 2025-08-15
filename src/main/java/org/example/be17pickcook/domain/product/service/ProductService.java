package org.example.be17pickcook.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.product.repository.ProductRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 등록
    public ProductDto.Res register(ProductDto.Register dto) {
        Product product = dto.toEntity();
        productRepository.save(product);
        return ProductDto.Res.from(product);
    }

    // 전체 조회
    public List<ProductDto.Res> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductDto.Res::from)
                .collect(Collectors.toList());
    }

    // 단건 조회
    public ProductDto.Res findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return ProductDto.Res.from(product);
    }

    // 수정
    public ProductDto.Res update(Long id, ProductDto.Update dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        dto.apply(product);
        productRepository.save(product);
        return ProductDto.Res.from(product);
    }

    // 삭제
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
