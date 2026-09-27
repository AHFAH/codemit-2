package com.example.boardapi.api;

import com.example.boardapi.api.dto.ApiDtos.CommentResponse;
import com.example.boardapi.api.dto.CommentRequest;
import com.example.boardapi.security.UserPrincipal;
import com.example.boardapi.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(201).body(commentService.create(principal, postId, request));
    }
    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponse> list(@PathVariable Long postId) { return commentService.list(postId); }
    @PatchMapping("/comments/{commentId}")
    public CommentResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long commentId,
                                  @Valid @RequestBody CommentRequest request) {
        return commentService.update(principal, commentId, request);
    }
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long commentId) {
        commentService.delete(principal, commentId);
        return ResponseEntity.noContent().build();
    }
}
