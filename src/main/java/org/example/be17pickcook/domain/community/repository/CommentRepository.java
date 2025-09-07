package org.example.be17pickcook.domain.community.repository;

import org.example.be17pickcook.domain.community.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPostId(Long postId);

    // 최상위 댓글 조회
    List<Comment> findByPostIdAndParentCommentIsNull(Long postId);


    // 게시글 ID 목록에 대한 댓글 개수 조회
    @Query("""
        SELECT c.post.id, COUNT(c.id)
        FROM Comment c
        WHERE c.post.id IN :postIds
        GROUP BY c.post.id
    """)
    List<Object[]> countCommentsByPostIds(@Param("postIds") List<Long> postIds);

}
