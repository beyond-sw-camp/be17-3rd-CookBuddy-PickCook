package org.example.be17pickcook.domain.community.repository;

import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    // 홈화면에서 사용할 데이터 반환
    @Query("""
    SELECT p.id, p.title, pi.imageUrl, p.user.nickname, p.user.profileImage, 
           p.likeCount, p.scrapCount, p.viewCount
    FROM Post p
    LEFT JOIN p.postImageList pi
    WHERE pi.id = (
        SELECT MIN(pi2.id) 
        FROM PostImage pi2 
        WHERE pi2.post = p
    )
""")
    Page<Object[]> findAllPostData(Pageable pageable);


    // 조회수 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.viewCount = coalesce(p.viewCount, 0) + 1 where p.id = :postId")
    int incrementViewCount(@Param("postId") Long postId);

    // 상세 조회
    @Query("""
        select distinct p from Post p
        left join fetch p.user u
        left join fetch p.postImageList i
        where p.id = :postId
        """)
    Optional<Post> findPostWithDetails(@Param("postId") Long postId);
}
