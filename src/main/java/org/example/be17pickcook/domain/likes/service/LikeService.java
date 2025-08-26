package org.example.be17pickcook.domain.likes.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.repository.CommentRepository;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.likes.model.LikeCountable;
import org.example.be17pickcook.domain.recipe.model.Recipe;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.likes.model.Like;
import org.example.be17pickcook.domain.likes.repository.LikeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private LikeCountable getTargetEntity(LikeTargetType targetType, Long targetId) {
        return switch (targetType) {
            case RECIPE -> recipeRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("레시피가 없습니다."));
            case POST -> postRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("커뮤니티 글이 없습니다."));
            case COMMENT -> commentRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));
        };
    }

    @Transactional
    public void toggleLike(UserDto.AuthUser authUser, LikeTargetType targetType, Long targetId) {
        Integer userIdx = authUser.getIdx();
        LikeCountable target = getTargetEntity(targetType, targetId);

        Optional<Like> existing = likeRepository.findByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, target.getIdx());

        if (existing.isPresent()) {
            // 좋아요 삭제
            likeRepository.delete(existing.get());
            target.decreaseLike();
        } else {
            // 좋아요 생성
            Like likes = Like.builder()
                    .user(User.builder().idx(userIdx).build())
                    .targetType(targetType)
                    .targetId(target.getIdx())
                    .build();
            likeRepository.save(likes);
            target.increaseLike();
        }
    }

    // 좋아요 개수 가져오기
    public int getLikeCount(LikeTargetType targetType, Long targetId) {
        return likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    // 사용자가 좋아요 눌렀는지 확인
    public boolean hasUserLiked(Integer userIdx, LikeTargetType targetType, Long targetId) {
        return likeRepository.existsByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, targetId);
    }
}
