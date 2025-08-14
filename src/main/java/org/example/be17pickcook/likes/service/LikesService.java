package org.example.be17pickcook.likes.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.example.be17pickcook.likes.model.LikeTargetType;
import org.example.be17pickcook.likes.model.Likes;
import org.example.be17pickcook.likes.repository.LikesRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikesService {
    private final LikesRepository likesRepository;

    @Transactional
    public void toggleLike(UserDto.AuthUser authUser, LikeTargetType targetType, Long targetId) {
        Integer userIdx = authUser.getIdx();

        Optional<Likes> existing = likesRepository.findByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, targetId);

        if (existing.isPresent()) {
            likesRepository.delete(existing.get());
        } else {
            Likes likes = Likes.builder()
                    .user(User.builder().idx(userIdx).build())
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            likesRepository.save(likes);
        }
    }
}
