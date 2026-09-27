package com.example.boardapi.repository;

import com.example.boardapi.domain.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = "author")
    List<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    @EntityGraph(attributePaths = {"author", "post"})
    Optional<Comment> findWithAuthorAndPostById(Long id);
}
