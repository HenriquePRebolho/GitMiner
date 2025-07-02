package aiss.gitminer.repository;

import aiss.gitminer.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository <Comment, Long> {
    Page<Comment> findByAuthor(String author, Pageable paging);
    Optional<Comment> findByBodyAndCreatedAt(String body, String createdAt);

    Page<Comment> findByCreatedAt(Pageable paging, String createdAt);
}
