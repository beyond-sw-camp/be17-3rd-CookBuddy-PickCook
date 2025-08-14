package org.example.be17pickcook.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.model.Comment;
import org.example.be17pickcook.domain.community.model.CommentDto;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.repository.CommentRepository;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글/대댓글 작성
    public CommentDto.Response createComment(CommentDto.Request request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) { // 대댓글일 경우 부모 댓글 확인
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
        }

        Comment comment = request.toEntity(user, post, parentComment);

        Comment saved = commentRepository.save(comment);
        return CommentDto.Response.fromEntity(saved);
    }

    // 게시글의 댓글 목록 조회 (대댓글 포함)
    public List<CommentDto.Response> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId);
        return comments.stream()
                .map(CommentDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
}