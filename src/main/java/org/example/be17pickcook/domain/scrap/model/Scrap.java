package org.example.be17pickcook.domain.scrap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.domain.user.model.User;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "scraps",
        uniqueConstraints =
                {@UniqueConstraint(columnNames =
                        {"user_id", "target_type", "target_id"})}
)
public class Scrap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScrapTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
