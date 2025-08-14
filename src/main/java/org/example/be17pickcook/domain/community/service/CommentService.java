package org.example.be17pickcook.domain.community.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.domain.community.model.Comment;
import org.example.be17pickcook.domain.community.model.CommentDto;
import org.example.be17pickcook.domain.community.model.Post;
import org.example.be17pickcook.domain.community.repository.CommentRepository;
import org.example.be17pickcook.domain.community.repository.PostRepository;
import org.example.be17pickcook.domain.user.model.User;
import org.example.be17pickcook.domain.user.model.UserDto;
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

    // 댓글 작성
    @Transactional
    public CommentDto.Response createComment(CommentDto.Request request, UserDto.AuthUser authUser) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findById(authUser.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = request.toEntity(post, user);
        Comment saved = commentRepository.save(comment);

        return CommentDto.Response.fromEntity(saved);
    }

    // 게시글 댓글 조회
    public List<CommentDto.Response> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(CommentDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
}