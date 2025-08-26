package org.example.be17pickcook.domain.likes.model;

public interface LikeCountable {
    Long getIdxLike();
    Long getLikeCount();
    void increaseLike();
    void decreaseLike();
}
