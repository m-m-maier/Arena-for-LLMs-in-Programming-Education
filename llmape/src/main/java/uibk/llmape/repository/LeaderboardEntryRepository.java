package uibk.llmape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uibk.llmape.domain.LeaderboardEntry;
import uibk.llmape.domain.enumeration.Category;

/**
 * Spring Data JPA repository for the LeaderboardEntry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {
	Optional<LeaderboardEntry> findFirstByCategoryOrderByTimestampDesc(Category category);
	Optional<LeaderboardEntry> findFirstByCategoryIsNullOrderByTimestampDesc();
}
