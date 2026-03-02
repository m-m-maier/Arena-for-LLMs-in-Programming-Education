package uibk.llmape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uibk.llmape.domain.Battle;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.repository.dto.BattlePairCountDTO;
import uibk.llmape.repository.dto.LeaderboardRowDTO;

/**
 * Spring Data JPA repository for the Battle entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
	@Query(
		"""
		    SELECT b FROM Battle b
		    JOIN FETCH b.prompt p
		    WHERE b.voteTimestamp IS NOT NULL
		    AND (:category IS NULL OR p.category = :category)
		"""
	)
	List<Battle> findAllWithVoteAndOptionalPromptCategory(@Param("category") Category category);

	@Query(
		"""
		    SELECT new uibk.llmape.repository.dto.LeaderboardRowDTO(
		        m.id,
		        m.modelName,
		        0.0f,
		        COUNT(b.id),
		        SUM(CASE WHEN b.id IS NOT NULL AND b.winnerModel IS NULL THEN 1 ELSE 0 END),
		        m.organization,
		        m.license
		    )
		    FROM Model m
		    LEFT JOIN Battle b ON (b.voteTimestamp IS NOT NULL
		        AND (m = b.model1 OR m = b.model2)
		        AND (b.model1.active = TRUE AND b.model2.active = TRUE))
		        AND (:category IS NULL OR EXISTS (
		        SELECT 1 FROM Prompt p2 WHERE p2 = b.prompt AND p2.category = :category
		        ))
		    WHERE m.active = TRUE
		    GROUP BY m.id, m.modelName, m.organization, m.license
		"""
	)
	List<LeaderboardRowDTO> getLeaderboardEntriesByCategory(@Param("category") Category category);

	@Query(
		"""
		SELECT new uibk.llmape.repository.dto.BattlePairCountDTO(
		    LEAST(b.model1.id, b.model2.id),
		    GREATEST(b.model1.id, b.model2.id),
		    COUNT(b)
		)
		FROM Battle b
		JOIN b.prompt p
		WHERE b.voteTimestamp IS NOT NULL
            AND p.category = :category
            AND b.model1.active = TRUE
            AND b.model2.active = TRUE
		GROUP BY LEAST(b.model1.id, b.model2.id), GREATEST(b.model1.id, b.model2.id)
		"""
	)
	List<BattlePairCountDTO> findVotedBattlePairCounts(@Param("category") Category category);
}
