package org.example.be17pickcook.domain.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.likes.model.LikeCountable;
import org.example.be17pickcook.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment extends BaseEntity implements LikeCountable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false, length = 500) // 길이 제한 설정
    private String content;
    @Column(nullable = false)
    private Long likeCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    @Override
    public Long getIdxLike() { return this.id; }
    @Override
    public Long getLikeCount() { return this.likeCount; }
    @Override
    public void increaseLike() {
        if (this.likeCount == null) this.likeCount = 0L;
        this.likeCount++;
    }
    @Override
    public void decreaseLike() {
        if (this.likeCount == null) this.likeCount = 0L;
        if (this.likeCount > 0) this.likeCount--;
    }
}
