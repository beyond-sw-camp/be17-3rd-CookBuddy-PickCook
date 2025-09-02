// 📁 org.example.be17pickcook.domain.user.mapper.AddressMapper.java

package org.example.be17pickcook.domain.user.mapper;

import org.example.be17pickcook.domain.user.model.Address;
import org.example.be17pickcook.domain.user.model.AddressDto;
import org.mapstruct.*;

import java.util.List;

/**
 * 배송지 매핑 인터페이스 (MapStruct)
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {

    // =================================================================
    // Request DTO → Entity 매핑 (추가용)
    // =================================================================

    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "user", ignore = true)  // Service에서 설정
    @Mapping(target = "createdAt", ignore = true)
    Address requestToEntity(AddressDto.Request dto);

    // =================================================================
    // Entity → Response DTO 매핑
    // =================================================================

    @Mapping(target = "fullAddress", expression = "java(entity.getFullAddress())")
    AddressDto.Response entityToResponse(Address entity);

    List<AddressDto.Response> entityListToResponseList(List<Address> entities);

    // =================================================================
    // Update DTO → Entity 매핑 (수정용)
    // =================================================================

    @Mapping(target = "addressId", source = "entity.addressId")
    @Mapping(target = "user", source = "entity.user")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "postalCode", expression = "java(dto.getPostalCode() != null ? dto.getPostalCode() : entity.getPostalCode())")
    @Mapping(target = "roadAddress", expression = "java(dto.getRoadAddress() != null ? dto.getRoadAddress() : entity.getRoadAddress())")
    @Mapping(target = "detailAddress", expression = "java(dto.getDetailAddress() != null ? dto.getDetailAddress() : entity.getDetailAddress())")
    @Mapping(target = "isDefault", expression = "java(dto.getIsDefault() != null ? dto.getIsDefault() : entity.getIsDefault())")
    Address updateEntityFromDto(Address entity, AddressDto.Update dto);
}