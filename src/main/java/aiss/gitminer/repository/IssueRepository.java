package aiss.gitminer.repository;

import aiss.gitminer.model.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    // Page<Issue> findById (String id);
    //Page<Issue> findByTitle(String title, Pageable paging);

    Page<Issue> findByState(String state, Pageable paging);
    // Page<Issue> findByAuthor (String author, Pageable pageable);
    // Page<Issue> findByState (String state, Pageable pageable);
    // ...
}
