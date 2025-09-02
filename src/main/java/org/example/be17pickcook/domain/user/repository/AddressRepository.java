// 📁 org.example.be17pickcook.domain.user.repository.AddressRepository.java

package org.example.be17pickcook.domain.user.repository;

import org.example.be17pickcook.domain.user.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 배송지 리포지토리
 * - 사용자별 배송지 CRUD
 * - 기본배송지 관리
 * - 중복 주소 확인
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    // =================================================================
    // 기본 조회 메서드
    // =================================================================

    /**
     * 사용자별 배송지 목록 조회 (기본배송지 우선 + 최신순)
     */
    List<Address> findByUserIdxOrderByIsDefaultDescCreatedAtDesc(Integer userId);

    /**
     * 사용자의 기본배송지 조회
     */
    Optional<Address> findByUserIdxAndIsDefaultTrue(Integer userId);

    /**
     * 사용자의 특정 배송지 조회 (권한 확인용)
     */
    Optional<Address> findByAddressIdAndUserIdx(Long addressId, Integer userId);

    // =================================================================
    // 중복 및 검증 메서드
    // =================================================================

    /**
     * 중복 주소 확인 (같은 사용자의 동일 주소)
     */
    boolean existsByUserIdxAndPostalCodeAndRoadAddressAndDetailAddress(
            Integer userId, String postalCode, String roadAddress, String detailAddress);

    /**
     * 사용자의 배송지 개수 조회
     */
    long countByUserIdx(Integer userId);

    // =================================================================
    // 기본배송지 관리 메서드
    // =================================================================

    /**
     * 사용자의 기본배송지 해제 (다른 배송지를 기본으로 설정하기 전)
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.idx = :userId AND a.isDefault = true")
    void clearDefaultByUserId(@Param("userId") Integer userId);

    /**
     * 기본배송지 삭제 시 대체할 가장 최근 배송지 찾기
     */
    Optional<Address> findTopByUserIdxAndAddressIdNotOrderByCreatedAtDesc(
            Integer userId, Long excludeAddressId);
}