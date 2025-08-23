package org.example.be17pickcook.domain.likes.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void toggleLike(UserDto.AuthUser authUser, LikeTargetType targetType, Long targetId) {
        Integer userIdx = authUser.getIdx();

        Optional<Like> existing = likeRepository.findByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, targetId);

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
        } else {
            Like likes = Like.builder()
                    .user(User.builder().idx(userIdx).build())
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            likeRepository.save(likes);
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
