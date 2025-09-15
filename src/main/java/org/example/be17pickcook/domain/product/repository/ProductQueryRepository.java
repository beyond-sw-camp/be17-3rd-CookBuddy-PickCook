package org.example.be17pickcook.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.be17pickcook.domain.cart.model.QCarts;
import org.example.be17pickcook.domain.product.model.ProductDto;
import org.example.be17pickcook.domain.product.model.QProduct;
import org.example.be17pickcook.domain.review.model.QReview;
import org.example.be17pickcook.domain.review.model.QReviewImage;
import org.example.be17pickcook.domain.user.model.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProductQueryRepository(EntityManager em) { this.queryFactory = new JPAQueryFactory(em); }

    public Page<ProductDto.ProductListResponse> getRecipesFiltered(
            String keyword, int page, int size, String dir, Integer userIdx) {

        QProduct product = QProduct.product;
        QCarts carts = QCarts.carts;
        QReview review = QReview.review;

        // 검색 조건
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            builder.and(product.title.containsIgnoreCase(keyword)
                    .or(product.subtitle.containsIgnoreCase(keyword)
                    .or(product.description.containsIgnoreCase(keyword))));
        }

        // 데이터 조회
        List<ProductDto.ProductListResponse> content = queryFactory
                .select(Projections.constructor(
                        ProductDto.ProductListResponse.class,
                        product.id,
                        product.title,
                        product.main_image_url,
                        product.discount_rate,
                        product.original_price,
                        carts.idx.isNotNull(),  // isInCart
                        review.countDistinct(), // review_count
                        review.rating.avg().coalesce(0.0) // average_rating
                ))
                .from(product)
                .leftJoin(carts)
                .on(carts.product.eq(product)
                        .and(carts.user.idx.eq(userIdx)))
                .leftJoin(review)
                .on(review.product.eq(product))
                .where(builder)
                .groupBy(product.id)
                .orderBy("DESC".equalsIgnoreCase(dir) ? product.createdAt.desc() : product.createdAt.asc())
                .offset(page * size)
                .limit(size)
                .fetch();

        // 전체 건수 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }



}
