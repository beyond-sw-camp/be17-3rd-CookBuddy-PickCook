package org.example.be17pickcook.domain.community.repository;

import org.example.be17pickcook.domain.community.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPostId(Long postId);

    // 최상위 댓글 조회
    List<Comment> findByPostIdAndParentCommentIsNull(Long postId);
}
