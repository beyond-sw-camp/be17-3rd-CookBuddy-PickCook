package org.example.be17pickcook.domain.community.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.model.PostDto;
import org.example.be17pickcook.domain.community.model.QComment;
import org.example.be17pickcook.domain.community.model.QPost;
import org.example.be17pickcook.domain.user.model.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PostQueryRepository(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<PostDto.ListResponse> findPostsWithPaging(String keyword, int page, int size, String dir) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QUser user = QUser.user;

        // 검색 조건
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isEmpty()) {
            builder.and(post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword)));
        }

        // 댓글 수 포함, 작성자 fetchJoin
        List<Tuple> results = queryFactory
                .select(post, comment.count())
                .from(post)
                .leftJoin(post.user, user).fetchJoin()
                .leftJoin(comment).on(comment.post.eq(post))
                .where(builder)
                .groupBy(post.id)
                .orderBy("DESC".equalsIgnoreCase(dir) ? post.createdAt.desc() : post.createdAt.asc())
                .offset(page * size)
                .limit(size)
                .fetch();

        // DTO 변환
        List<PostDto.ListResponse> content = results.stream()
                .map(tuple -> {
                    Post postEntity = tuple.get(post);
                    long commentCount = tuple.get(comment.count());
                    return PostDto.ListResponse.from(postEntity, (int) commentCount);
                })
                .collect(Collectors.toList());

        // 전체 건수
        long total = queryFactory.selectFrom(post)
                .where(builder)
                .fetchCount();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total);
    }
}