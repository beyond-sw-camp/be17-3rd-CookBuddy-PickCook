package org.example.be17pickcook.domain.community.repository;

import org.example.be17pickcook.domain.community.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
}
