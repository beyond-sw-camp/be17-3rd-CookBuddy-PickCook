package org.example.be17pickcook.refrigerator.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.be17pickcook.refrigerator.model.Refrigerator;
import org.example.be17pickcook.refrigerator.model.RefrigeratorItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RefrigeratorItemDto {

    // ================== 응답 DTO ==================
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRes {
        private Long id;
        private String ingredientName;
        private String quantity;
        private LocalDate expirationDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static ItemRes from(RefrigeratorItem entity) {
            return ItemRes.builder()
                    .id(entity.getId())
                    .ingredientName(entity.getIngredientName())
                    .quantity(entity.getQuantity())
                    .expirationDate(entity.getExpirationDate())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }

    // ================== 등록 DTO ==================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Register {
        @NotBlank
        private String ingredientName;

        @Size(max = 50)
        private String quantity;

        @NotNull
        private LocalDate expirationDate;

        public RefrigeratorItem toEntity(Refrigerator refrigerator) {
            return RefrigeratorItem.builder()
                    .refrigerator(refrigerator)
                    .ingredientName(ingredientName)
                    .quantity(quantity)
                    .expirationDate(expirationDate)
                    .build();
        }
    }

    // ================== 수정 DTO ==================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String quantity;
        private LocalDate expirationDate;

        public void apply(RefrigeratorItem entity) {
            if (quantity != null) entity.changeQuantity(quantity);
            if (expirationDate != null) entity.changeExpirationDate(expirationDate);
        }
    }
}
