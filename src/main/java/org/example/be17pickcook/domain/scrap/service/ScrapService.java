package org.example.be17pickcook.domain.scrap.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.scrap.model.ScrapTargetType;
import org.example.be17pickcook.domain.scrap.model.Scrap;
import org.example.be17pickcook.domain.scrap.repository.ScrapRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;

    @Transactional
    public void toggleScrap(UserDto.AuthUser authUser, ScrapTargetType targetType, Long targetId) {
        Integer userIdx = authUser.getIdx();

        Optional<Scrap> existing = scrapRepository.findByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, targetId);

        if (existing.isPresent()) {
            scrapRepository.delete(existing.get());
        } else {
            Scrap scrap = Scrap.builder()
                    .user(User.builder().idx(userIdx).build())
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            scrapRepository.save(scrap);
        }
    }

    // 좋아요 개수 가져오기
    public int getScrapCount(ScrapTargetType targetType, Long targetId) {
        return scrapRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    // 사용자가 좋아요 눌렀는지 확인
    public boolean hasUserScrapped(Integer userIdx, ScrapTargetType targetType, Long targetId) {
        return scrapRepository.existsByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, targetId);
    }
}
