package org.example.be17pickcook.domain.review.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.domain.product.model.Product;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Review Entity
 * 리뷰 정보 (리뷰 제목, 내용, 평점, 작성자, 상품, 상태 등)
 */
@Entity
@Table(name = "review") // DB 테이블명 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT PK
    @Column(name = "review_id") // DB 컬럼명 명시
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;     // 리뷰 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;   // 리뷰 내용

    @Column(nullable = false)
    private int rating;       // 평점 (1~5)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;    // VISIBLE, HIDDEN

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 리뷰 작성자 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK: user.user_id
    private User user;

    // 어떤 상품에 대한 리뷰인지 (Product)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // FK: product.product_id
    private Product product;

    // 상태 Enum
    public enum Status {
        VISIBLE,
        HIDDEN
    }
}
