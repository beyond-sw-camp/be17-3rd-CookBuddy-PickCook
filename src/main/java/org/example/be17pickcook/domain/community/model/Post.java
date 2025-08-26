package org.example.be17pickcook.domain.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.likes.model.LikeCountable;
import org.example.be17pickcook.domain.user.model.User;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post extends BaseEntity implements LikeCountable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    private Long likeCount;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Override
    public Long getIdx() { return this.id; }
    @Override
    public Long getLikeCount() { return this.likeCount; }
    @Override
    public void increaseLike() { this.likeCount++; }
    @Override
    public void decreaseLike() { if (this.likeCount > 0) this.likeCount--;}
}
