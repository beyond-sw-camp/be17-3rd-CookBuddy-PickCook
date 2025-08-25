package org.example.be17pickcook.domain.cart.service;

import jakarta.transaction.Transactional;
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
    public void register(UserDto.AuthUser authUser, CartsDto.CartsRequestDto dto) {
        User user = User.builder().idx(authUser.getIdx()).build();

        for (Carts cartItem : dto.toEntity(user)) {
            Long productId = cartItem.getProduct().getId();

            Optional<Carts> existingOpt = cartsRepository.findByUserIdxAndProductId(user.getIdx(), productId);

            if (existingOpt.isPresent()) {
                // 이미 장바구니에 등록된 상품이라면 수량 합산
                Carts existing = existingOpt.get();
                int newQuantity = existing.getQuantity() + (cartItem.getQuantity() != null ? cartItem.getQuantity() : 1);
                existing.updateQuantity(newQuantity);
                cartsRepository.save(existing);
            } else {
                cartsRepository.save(cartItem);
            }
        }
    }


    // 장바구니 삭제
    public void delete(UserDto.AuthUser authUser, CartsDto.CartsRequestDto dto) {
        User user = User.builder().idx(authUser.getIdx()).build();

        for (Long procutId : dto.getProduct_ids()) {
            Optional<Carts> existingOpt = cartsRepository.findByUserIdxAndProductId(user.getIdx(), procutId);

            existingOpt.ifPresent(cartsRepository::delete);  // 있으면 삭제, 없으면 아무것도 안 함
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

    // 장바구니 수량 변경
    @Transactional
    public void updateQuantity(UserDto.AuthUser authUser, Long cartItemId, Integer quantity) {
        Carts cartItem = cartsRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목이 존재하지 않습니다."));

        // 본인 장바구니인지 검증 (보안)
        if (!cartItem.getUser().getIdx().equals(authUser.getIdx())) {
            throw new SecurityException("본인의 장바구니만 수정할 수 있습니다.");
        }

        // 수량 변경
        cartItem.updateQuantity(quantity);
    }
}
