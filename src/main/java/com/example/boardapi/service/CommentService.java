package com.example.boardapi.service;

import com.example.boardapi.api.dto.ApiDtos.CommentResponse;
import com.example.boardapi.api.dto.CommentRequest;
import com.example.boardapi.api.error.ApiException;
import com.example.boardapi.domain.Comment;
import com.example.boardapi.domain.Member;
import com.example.boardapi.domain.Post;
import com.example.boardapi.repository.CommentRepository;
import com.example.boardapi.repository.MemberRepository;
import com.example.boardapi.repository.PostRepository;
import com.example.boardapi.security.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse create(UserPrincipal principal, Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(() -> postNotFound());
        Member author = memberRepository.getReferenceById(principal.id());
        Comment comment = commentRepository.save(new Comment(post, author, request.content().trim()));
        return CommentResponse.from(commentRepository.findWithAuthorAndPostById(comment.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        if (!postRepository.existsById(postId)) throw postNotFound();
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId).stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse update(UserPrincipal principal, Long id, CommentRequest request) {
        Comment comment = findComment(id);
        checkOwner(comment, principal);
        comment.update(request.content().trim());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Comment comment = findComment(id);
        checkOwner(comment, principal);
        commentRepository.delete(comment);
    }

    private Comment findComment(Long id) {
        return commentRepository.findWithAuthorAndPostById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."));
    }
    private void checkOwner(Comment comment, UserPrincipal principal) {
        if (!comment.getAuthor().getId().equals(principal.id())) throw new AccessDeniedException("요청을 수행할 권한이 없습니다.");
    }
    private ApiException postNotFound() { return new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."); }
}
