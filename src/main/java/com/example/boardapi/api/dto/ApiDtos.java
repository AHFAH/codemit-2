package com.example.boardapi.api.dto;

import com.example.boardapi.domain.Comment;
import com.example.boardapi.domain.Member;
import com.example.boardapi.domain.Post;
import com.example.boardapi.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record MemberResponse(Long id, String email, String nickname, LocalDateTime createdAt) {
        public static MemberResponse from(Member member) {
            return new MemberResponse(member.getId(), member.getEmail(), member.getNickname(), member.getCreatedAt());
        }
    }
    public record SessionResponse(Long memberId, String email, String nickname, String message) {}
    public record PostResponse(Long id, String title, String content, Long authorId, String authorNickname,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static PostResponse from(Post post) {
            return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor().getId(),
                    post.getAuthor().getNickname(), post.getCreatedAt(), post.getUpdatedAt());
        }
    }
    public record PostSummaryResponse(Long id, String title, String authorNickname, Long commentCount,
                                      LocalDateTime createdAt) {
        public static PostSummaryResponse from(PostRepository.PostSummary summary) {
            return new PostSummaryResponse(summary.getId(), summary.getTitle(), summary.getAuthorNickname(),
                    summary.getCommentCount(), summary.getCreatedAt());
        }
    }
    public record CommentResponse(Long id, Long postId, Long authorId, String authorNickname, String content,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static CommentResponse from(Comment comment) {
            return new CommentResponse(comment.getId(), comment.getPost().getId(), comment.getAuthor().getId(),
                    comment.getAuthor().getNickname(), comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt());
        }
    }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}
