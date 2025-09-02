package org.example.be17pickcook.domain.community.repository;

import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
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
}
