package org.example.be17pickcook.product.P_Controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.product.P_Service.ProductService;
import org.example.be17pickcook.product.model.ProductDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductDto.Res register(@RequestBody ProductDto.Register dto) {
        return productService.register(dto);
    }

    @GetMapping
    public List<ProductDto.Res> getAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductDto.Res getOne(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public ProductDto.Res update(@PathVariable Long id, @RequestBody ProductDto.Update dto) {
        return productService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
