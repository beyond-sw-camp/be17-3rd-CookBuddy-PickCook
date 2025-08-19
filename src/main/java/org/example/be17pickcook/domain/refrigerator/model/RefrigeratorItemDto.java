package org.example.be17pickcook.domain.refrigerator.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RefrigeratorItemDto {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemRes {
        private Long id;
        private String ingredientName;
        private String quantity;
        private LocalDate expirationDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static ItemRes from(RefrigeratorItem e) {
            return ItemRes.builder()
                    .id(e.getId())
                    .ingredientName(e.getIngredientName())
                    .quantity(e.getQuantity())
                    .expirationDate(e.getExpirationDate())
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .build();
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Register {
        @NotBlank private String ingredientName; // 최소 검증만
        @NotBlank private String quantity;       // 최소 검증만
        private LocalDate expirationDate;        // null 허용

        public RefrigeratorItem toEntity(Refrigerator refrigerator) {
            return RefrigeratorItem.builder()
                    .refrigerator(refrigerator)
                    .ingredientName(ingredientName)
                    .quantity(quantity)
                    .expirationDate(expirationDate)
                    .build();
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Update {
        private String ingredientName;  // 선택 수정
        private String quantity;        // 선택 수정
        private LocalDate expirationDate;

        public void apply(RefrigeratorItem entity) {
            if (ingredientName != null) entity.changeIngredientName(ingredientName);
            if (quantity != null) entity.changeQuantity(quantity);
            if (expirationDate != null) entity.changeExpirationDate(expirationDate);
        }
    }
}
