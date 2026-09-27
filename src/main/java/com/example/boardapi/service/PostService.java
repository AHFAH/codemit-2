package com.example.boardapi.service;

import com.example.boardapi.api.dto.ApiDtos.PageResponse;
import com.example.boardapi.api.dto.ApiDtos.PostResponse;
import com.example.boardapi.api.dto.ApiDtos.PostSummaryResponse;
import com.example.boardapi.api.dto.PostRequest;
import com.example.boardapi.api.error.ApiException;
import com.example.boardapi.domain.Member;
import com.example.boardapi.domain.Post;
import com.example.boardapi.repository.MemberRepository;
import com.example.boardapi.repository.PostRepository;
import com.example.boardapi.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostResponse create(UserPrincipal principal, PostRequest request) {
        Member author = memberRepository.getReferenceById(principal.id());
        return PostResponse.from(postRepository.save(new Post(author, request.title().trim(), request.content().trim())));
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "page는 0 이상, size는 1~100이어야 합니다.");
        Page<PostSummaryResponse> result = postRepository.findSummaries(PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))).map(PostSummaryResponse::from);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) { return PostResponse.from(findPost(id)); }

    @Transactional
    public PostResponse update(UserPrincipal principal, Long id, PostRequest request) {
        Post post = findPost(id);
        checkOwner(post.getAuthor().getId(), principal.id());
        post.update(request.title().trim(), request.content().trim());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Post post = findPost(id);
        checkOwner(post.getAuthor().getId(), principal.id());
        postRepository.delete(post);
    }

    private Post findPost(Long id) {
        return postRepository.findWithAuthorById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
    }
    private void checkOwner(Long ownerId, Long memberId) {
        if (!ownerId.equals(memberId)) throw new AccessDeniedException("요청을 수행할 권한이 없습니다.");
    }
}
