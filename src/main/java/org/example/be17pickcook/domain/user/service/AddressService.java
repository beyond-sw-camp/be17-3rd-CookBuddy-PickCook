// 📁 org.example.be17pickcook.domain.user.service.AddressService.java

package org.example.be17pickcook.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.exception.BaseException;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.user.mapper.AddressMapper;
import org.example.be17pickcook.domain.user.model.Address;
import org.example.be17pickcook.domain.user.model.AddressDto;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.AddressRepository;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 배송지 서비스
 * - 배송지 CRUD 관리
 * - 기본배송지 자동 관리 로직
 * - 중복 배송지 검증
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    /**
     * 사용자별 배송지 목록 조회
     */
    public List<AddressDto.Response> getAddressesByUserId(Integer userId) {
        validateUserExists(userId);

        List<Address> addresses = addressRepository
                .findByUserIdxOrderByIsDefaultDescCreatedAtDesc(userId);

        return addressMapper.entityListToResponseList(addresses);
    }

    /**
     * 특정 배송지 조회
     */
    public AddressDto.Response getAddressById(Long addressId, Integer userId) {
        validateUserExists(userId);
        Address address = findAddressByIdAndUserId(addressId, userId);
        return addressMapper.entityToResponse(address);
    }

    /**
     * 배송지 추가
     */
    @Transactional
    public AddressDto.Response createAddress(AddressDto.Request dto, Integer userId) {
        // 사용자 존재 확인
        User user = findUserById(userId);

        // 중복 주소 확인
        validateDuplicateAddress(userId, dto.getPostalCode(),
                dto.getRoadAddress(), dto.getDetailAddress());

        // DTO → Entity 변환
        Address entity = addressMapper.requestToEntity(dto);
        entity = entity.toBuilder()
                .user(user)
                .build();

        // 첫 번째 배송지이거나 기본배송지 설정 요청 시 기본배송지 관리
        if (dto.getIsDefault() || isFirstAddress(userId)) {
            manageDefaultAddress(userId, true);
            entity = entity.toBuilder()
                    .isDefault(true)
                    .build();
        }

        // 저장
        Address savedEntity = addressRepository.save(entity);
        return addressMapper.entityToResponse(savedEntity);
    }

    /**
     * 배송지 수정
     */
    @Transactional
    public AddressDto.Response updateAddress(Long addressId, AddressDto.Update dto, Integer userId) {
        validateUserExists(userId);
        Address existingAddress = findAddressByIdAndUserId(addressId, userId);

        // 주소 변경 시 중복 확인
        if (isAddressChanged(dto, existingAddress)) {
            String newPostalCode = dto.getPostalCode() != null ?
                    dto.getPostalCode() : existingAddress.getPostalCode();
            String newRoadAddress = dto.getRoadAddress() != null ?
                    dto.getRoadAddress() : existingAddress.getRoadAddress();
            String newDetailAddress = dto.getDetailAddress() != null ?
                    dto.getDetailAddress() : existingAddress.getDetailAddress();

            validateDuplicateAddressExcludingSelf(userId, addressId,
                    newPostalCode, newRoadAddress, newDetailAddress);
        }

        // 기본배송지 변경 처리
        if (dto.getIsDefault() != null) {
            if (dto.getIsDefault()) {
                manageDefaultAddress(userId, true);
            } else if (existingAddress.getIsDefault()) {
                // 기본배송지를 일반배송지로 변경 시, 다른 배송지를 기본으로 설정
                setAlternativeDefaultAddress(userId, addressId);
            }
        }

        // Entity 업데이트
        Address updatedAddress = addressMapper.updateEntityFromDto(existingAddress, dto);

        // 기본배송지 설정 반영
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            existingAddress.setAsDefault();
        } else if (dto.getIsDefault() != null && !dto.getIsDefault()
                && existingAddress.getIsDefault()) {
            existingAddress.removeDefault();
        }

        Address savedEntity = addressRepository.save(updatedAddress);
        return addressMapper.entityToResponse(savedEntity);
    }

    /**
     * 배송지 삭제
     */
    @Transactional
    public void deleteAddress(Long addressId, Integer userId) {
        validateUserExists(userId);
        Address address = findAddressByIdAndUserId(addressId, userId);

        // 기본배송지 삭제 시 대체 기본배송지 설정
        if (address.getIsDefault()) {
            setAlternativeDefaultAddress(userId, addressId);
        }

        addressRepository.delete(address);
    }

    // =================================================================
    // 기본배송지 자동 관리 로직
    // =================================================================

    /**
     * 기본배송지 관리
     * @param userId 사용자 ID
     * @param setDefault true면 기존 기본배송지 해제
     */
    private void manageDefaultAddress(Integer userId, boolean setDefault) {
        if (setDefault) {
            // 기존 기본배송지 모두 해제
            addressRepository.clearDefaultByUserId(userId);
        }
    }

    /**
     * 대체 기본배송지 설정 (기본배송지 삭제 시)
     */
    private void setAlternativeDefaultAddress(Integer userId, Long excludeAddressId) {
        addressRepository.findTopByUserIdxAndAddressIdNotOrderByCreatedAtDesc(userId, excludeAddressId)
                .ifPresent(alternativeAddress -> {
                    alternativeAddress.setAsDefault();
                    addressRepository.save(alternativeAddress);
                });
    }

    /**
     * 첫 번째 배송지인지 확인
     */
    private boolean isFirstAddress(Integer userId) {
        return addressRepository.countByUserIdx(userId) == 0;
    }

    // =================================================================
    // 검증 관련 메서드
    // =================================================================

    /**
     * 중복 주소 확인
     */
    private void validateDuplicateAddress(Integer userId, String postalCode,
                                          String roadAddress, String detailAddress) {
        if (addressRepository.existsByUserIdxAndPostalCodeAndRoadAddressAndDetailAddress(
                userId, postalCode, roadAddress, detailAddress)) {
            throw BaseException.from(BaseResponseStatus.ADDRESS_DUPLICATE_ERROR);
        }
    }

    /**
     * 중복 주소 확인 (본인 배송지 제외)
     */
    private void validateDuplicateAddressExcludingSelf(Integer userId, Long excludeAddressId,
                                                       String postalCode, String roadAddress, String detailAddress) {
        List<Address> existingAddresses = addressRepository.findByUserIdxOrderByIsDefaultDescCreatedAtDesc(userId);

        boolean isDuplicate = existingAddresses.stream()
                .anyMatch(address ->
                        !address.getAddressId().equals(excludeAddressId) &&
                                address.getPostalCode().equals(postalCode) &&
                                address.getRoadAddress().equals(roadAddress) &&
                                address.getDetailAddress().equals(detailAddress)
                );

        if (isDuplicate) {
            throw BaseException.from(BaseResponseStatus.ADDRESS_DUPLICATE_ERROR);
        }
    }

    /**
     * 주소 변경 여부 확인
     */
    private boolean isAddressChanged(AddressDto.Update dto, Address existingAddress) {
        return (dto.getPostalCode() != null && !dto.getPostalCode().equals(existingAddress.getPostalCode())) ||
                (dto.getRoadAddress() != null && !dto.getRoadAddress().equals(existingAddress.getRoadAddress())) ||
                (dto.getDetailAddress() != null && !dto.getDetailAddress().equals(existingAddress.getDetailAddress()));
    }

    // =================================================================
    // 유틸리티 메서드들
    // =================================================================

    /**
     * 사용자 ID로 User 엔티티 조회
     */
    private User findUserById(Integer userId) {
        return userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.USER_NOT_FOUND));
    }

    /**
     * 사용자 존재 확인
     */
    private void validateUserExists(Integer userId) {
        if (!userRepository.existsByIdAndNotDeleted(userId)) {
            throw BaseException.from(BaseResponseStatus.USER_NOT_FOUND);
        }
    }

    /**
     * 배송지 ID와 사용자 ID로 배송지 조회 (권한 확인)
     */
    private Address findAddressByIdAndUserId(Long addressId, Integer userId) {
        return addressRepository.findByAddressIdAndUserIdx(addressId, userId)
                .orElseThrow(() -> BaseException.from(BaseResponseStatus.ADDRESS_NOT_FOUND));
    }
}