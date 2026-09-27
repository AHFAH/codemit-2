package com.example.boardapi.api;

import com.example.boardapi.api.dto.ApiDtos.PageResponse;
import com.example.boardapi.api.dto.ApiDtos.PostResponse;
import com.example.boardapi.api.dto.ApiDtos.PostSummaryResponse;
import com.example.boardapi.api.dto.PostRequest;
import com.example.boardapi.security.UserPrincipal;
import com.example.boardapi.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PostRequest request) {
        return ResponseEntity.status(201).body(postService.create(principal, request));
    }
    @GetMapping
    public PageResponse<PostSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return postService.list(page, size);
    }
    @GetMapping("/{postId}")
    public PostResponse get(@PathVariable Long postId) { return postService.get(postId); }
    @PutMapping("/{postId}")
    public PostResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long postId,
                               @Valid @RequestBody PostRequest request) {
        return postService.update(principal, postId, request);
    }
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long postId) {
        postService.delete(principal, postId);
        return ResponseEntity.noContent().build();
    }
}
