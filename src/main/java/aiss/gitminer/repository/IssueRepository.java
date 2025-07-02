package aiss.gitminer.repository;

import aiss.gitminer.model.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    Page<Issue> findByState(String state, Pageable paging);
    Optional<Issue> findByTitleAndCreatedAt(String title, String createdAt);

    Page<Issue> findByStateAndCreatedAt(String state, String createdAt, Pageable paging);

    Page<Issue> findByStateAndAuthorId(String state, Long authorId, Pageable paging);

    Page<Issue> findByCreatedAt(String createdAt, Pageable paging);
}
