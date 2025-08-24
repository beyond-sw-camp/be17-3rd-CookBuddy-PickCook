package org.example.be17pickcook.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.cart.model.Carts;
import org.example.be17pickcook.domain.cart.model.CartsDto;
import org.example.be17pickcook.domain.cart.repository.CartsRepository;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartsService {
    private final CartsRepository cartsRepository;

    // 장바구니 등록
    public void toggleInCart(UserDto.AuthUser authUser,
                         CartsDto.CartsRequestDto dto) {
        Integer userIdx = authUser.getIdx();

        for (Long productId : dto.getProduct_ids()) {
            Optional<Carts> existing = cartsRepository.findByUserIdxAndProductId(userIdx, productId);

            if (existing.isPresent()) {
                cartsRepository.delete(existing.get());
            } else {
                Carts item = Carts.builder()
                        .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                        .product(Product.builder().id(productId).build())
                        .user(User.builder().idx(authUser.getIdx()).build())
                        .build();
                cartsRepository.save(item);
            }
        }
    }

    // 장바구니 목록 조회
    public List<CartsDto.CartsResponseDto> getCarts(Integer userIdx) {
        // 특정 유저의 장바구니만 가져오기
        List<Carts> carts = cartsRepository.findByUserIdx(userIdx);

        // entity 리스트 -> dto 리스트 변환
        return carts.stream()
                .map(CartsDto.CartsResponseDto::fromEntity)
                .toList();
    }
}
