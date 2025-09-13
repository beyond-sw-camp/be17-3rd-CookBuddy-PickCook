package org.example.be17pickcook.domain.community.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.be17pickcook.common.BaseEntity;
import org.example.be17pickcook.domain.likes.model.LikeCountable;
import org.example.be17pickcook.domain.scrap.model.ScrapCountable;
import org.example.be17pickcook.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post extends BaseEntity implements LikeCountable, ScrapCountable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    @Column(columnDefinition = "TEXT")
    @Setter
    private String title;
    @Column(columnDefinition = "TEXT")
    @Setter
    private String content;
    private Long likeCount;
    private Long scrapCount;
    private Long viewCount;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImageList;


    public void addImageUrl(PostImage image) {
        if (this.postImageList == null) this.postImageList = new ArrayList<>();

        this.postImageList.add(image);
        image.setPost(this);
    }

    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0L;
        }
        this.viewCount++;
    }

    // 좋아요
    @Override
    public Long getIdxLike() { return this.id; }
    @Override
    public Long getLikeCount() { return this.likeCount; }
    @Override
    public void increaseLike() {
        if (likeCount == null) {
            likeCount = 0L;
        }
        likeCount++;
    }
    @Override
    public void decreaseLike() {
        if (likeCount == null || likeCount <= 0) {
            likeCount = 0L;
        } else {
            likeCount--;
        }
    }


    // 스크랩
    @Override
    public Long getIdxScrap() { return this.id; }
    @Override
    public Long getScrapCount() { return this.scrapCount; }
    @Override
    public void increaseScrap() {
        if (scrapCount == null) {
            scrapCount = 0L;
        }
        scrapCount++;
    }
    @Override
    public void decreaseScrap() {
        if (scrapCount == null || scrapCount <= 0) {
            scrapCount = 0L;
        } else {
            scrapCount--;
        }
    }
}
