package uibk.llmape.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uibk.llmape.domain.Prompt;

/**
 * Spring Data JPA repository for the Prompt entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {}
