package com.example.boardapi.repository;

import com.example.boardapi.domain.Post;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "select p.id as id, p.title as title, m.nickname as authorNickname, p.createdAt as createdAt, " +
            "(select count(c.id) from Comment c where c.post = p) as commentCount " +
            "from Post p join p.author m",
            countQuery = "select count(p) from Post p")
    Page<PostSummary> findSummaries(Pageable pageable);

    @Query("select p from Post p join fetch p.author where p.id = :id")
    Optional<Post> findWithAuthorById(@Param("id") Long id);

    interface PostSummary {
        Long getId();
        String getTitle();
        String getAuthorNickname();
        LocalDateTime getCreatedAt();
        Long getCommentCount();
    }
}
