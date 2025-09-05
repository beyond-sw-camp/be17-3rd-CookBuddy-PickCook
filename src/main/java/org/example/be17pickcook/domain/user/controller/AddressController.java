package org.example.be17pickcook.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.BaseResponseStatus;
import org.example.be17pickcook.domain.user.model.AddressDto;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.user.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 배송지 관리 컨트롤러
 * - 사용자 배송지 CRUD API 제공
 * - 기본배송지 관리 기능
 * - JWT 인증 기반 사용자별 배송지 관리
 */
@Tag(name = "배송지 관리", description = "사용자 배송지 CRUD 관리 API")
@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    // =================================================================
    // 의존성 주입
    // =================================================================

    private final AddressService addressService;

    // =================================================================
    // 기본 CRUD 관련 API
    // =================================================================

    /**
     * 배송지 목록 조회
     */
    @Operation(
            summary = "배송지 목록 조회",
            description = "로그인한 사용자의 모든 배송지를 조회합니다. 기본배송지가 우선 표시됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping
    public ResponseEntity<BaseResponse<List<AddressDto.Response>>> getAddresses(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        List<AddressDto.Response> addresses = addressService.getAddressesByUserId(authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(addresses, BaseResponseStatus.ADDRESS_LIST_SUCCESS));
    }

    /**
     * 특정 배송지 조회
     */
    @Operation(
            summary = "특정 배송지 조회",
            description = "배송지 ID로 특정 배송지 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "배송지를 찾을 수 없음")
            }
    )
    @GetMapping("/{addressId}")
    public ResponseEntity<BaseResponse<AddressDto.Response>> getAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long addressId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        AddressDto.Response address = addressService.getAddressById(addressId, authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(address));
    }

    /**
     * 배송지 추가
     */
    @Operation(
            summary = "배송지 추가",
            description = "새로운 배송지를 추가합니다. 첫 번째 배송지는 자동으로 기본배송지로 설정됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추가 성공"),
                    @ApiResponse(responseCode = "400", description = "입력값 오류"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "409", description = "중복된 주소")
            }
    )
    @PostMapping
    public ResponseEntity<BaseResponse<AddressDto.Response>> createAddress(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "배송지 추가 요청 정보")
            @Valid @RequestBody AddressDto.Request dto,
            BindingResult bindingResult) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        AddressDto.Response result = addressService.createAddress(dto, authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.ADDRESS_CREATE_SUCCESS));
    }

    /**
     * 배송지 수정
     */
    @Operation(
            summary = "배송지 수정",
            description = "기존 배송지 정보를 수정합니다. 기본배송지 변경도 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력값 오류"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "배송지를 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "중복된 주소")
            }
    )
    @PutMapping("/{addressId}")
    public ResponseEntity<BaseResponse<AddressDto.Response>> updateAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long addressId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser,
            @Parameter(description = "배송지 수정 요청 정보")
            @Valid @RequestBody AddressDto.Update dto,
            BindingResult bindingResult) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // Validation 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(BaseResponseStatus.REQUEST_ERROR, errorMessage));
        }

        // GlobalExceptionHandler가 예외 처리
        AddressDto.Response result = addressService.updateAddress(addressId, dto, authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(result, BaseResponseStatus.ADDRESS_UPDATE_SUCCESS));
    }

    /**
     * 배송지 삭제
     */
    @Operation(
            summary = "배송지 삭제",
            description = "기존 배송지를 삭제합니다. 기본배송지 삭제 시 다른 배송지가 자동으로 기본배송지로 설정됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "배송지를 찾을 수 없음")
            }
    )
    @DeleteMapping("/{addressId}")
    public ResponseEntity<BaseResponse<Void>> deleteAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long addressId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // GlobalExceptionHandler가 예외 처리
        addressService.deleteAddress(addressId, authUser.getIdx());
        return ResponseEntity.ok(BaseResponse.success(null, BaseResponseStatus.ADDRESS_DELETE_SUCCESS));
    }

    // =================================================================
    // 기타 비즈니스 로직 API (선택사항)
    // =================================================================

    /**
     * 기본배송지 조회 (선택사항)
     */
    @Operation(
            summary = "기본배송지 조회",
            description = "사용자의 기본배송지 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "기본배송지가 없음")
            }
    )
    @GetMapping("/default")
    public ResponseEntity<BaseResponse<AddressDto.Response>> getDefaultAddress(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error(BaseResponseStatus.UNAUTHORIZED));
        }

        // 기본배송지만 조회하는 서비스 메서드가 필요하다면 추가 구현
        List<AddressDto.Response> addresses = addressService.getAddressesByUserId(authUser.getIdx());

        AddressDto.Response defaultAddress = addresses.stream()
                .filter(AddressDto.Response::getIsDefault)
                .findFirst()
                .orElse(null);

        if (defaultAddress == null) {
            return ResponseEntity.ok(BaseResponse.error(BaseResponseStatus.ADDRESS_DEFAULT_NOT_FOUND));
        }

        return ResponseEntity.ok(BaseResponse.success(defaultAddress));
    }
}