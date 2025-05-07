package aiss.gitminer.repository;

import aiss.gitminer.model.Project;
import aiss.gitminer.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByName (String name, Pageable pageable); // metodo de parametros opcionales
    // Page<User> findByNameContaining (String name, Pageable pageable);
    // ...
}
