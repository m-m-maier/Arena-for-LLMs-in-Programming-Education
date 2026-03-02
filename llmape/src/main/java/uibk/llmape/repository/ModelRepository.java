package uibk.llmape.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uibk.llmape.domain.Model;

import java.util.List;

/**
 * Spring Data JPA repository for the Model entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findAllByActiveTrue();
}
