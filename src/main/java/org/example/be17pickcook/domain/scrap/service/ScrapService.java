package org.example.be17pickcook.domain.scrap.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.repository.CommentRepository;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.likes.model.LikeCountable;
import org.example.be17pickcook.domain.likes.model.LikeTargetType;
import org.example.be17pickcook.domain.recipe.repository.RecipeRepository;
import org.example.be17pickcook.domain.scrap.model.ScrapCountable;
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
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private ScrapCountable getTargetEntity(ScrapTargetType targetType, Long targetId) {
        return switch (targetType) {
            case RECIPE -> recipeRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("레시피가 없습니다."));
            case POST -> postRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("커뮤니티 글이 없습니다."));
        };
    }

    @Transactional
    public void toggleScrap(UserDto.AuthUser authUser, ScrapTargetType targetType, Long targetId) {
        Integer userIdx = authUser.getIdx();
        ScrapCountable target = getTargetEntity(targetType, targetId);

        Optional<Scrap> existing = scrapRepository.findByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, target.getIdxScrap());

        if (existing.isPresent()) {
            // 스크랩 삭제
            scrapRepository.delete(existing.get());
            target.decreaseScrap();
        } else {
            // 스크랩 생성
            Scrap scrap = Scrap.builder()
                    .user(User.builder().idx(userIdx).build())
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            scrapRepository.save(scrap);
            target.increaseScrap();
        }
    }

    // 스크랩 개수 가져오기
    public int getScrapCount(ScrapTargetType targetType, Long targetId) {
        return scrapRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    // 사용자가 스크랩 눌렀는지 확인
    public boolean hasUserScrapped(Integer userIdx, ScrapTargetType targetType, Long targetId) {
        return scrapRepository.existsByUserIdxAndTargetTypeAndTargetId(userIdx, targetType, targetId);
    }
}
