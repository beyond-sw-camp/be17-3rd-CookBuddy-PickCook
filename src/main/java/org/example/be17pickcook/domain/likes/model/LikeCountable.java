package org.example.be17pickcook.domain.likes.model;

public interface LikeCountable {
    Long getIdx();
    Long getLikeCount();
    void increaseLike();
    void decreaseLike();
}
