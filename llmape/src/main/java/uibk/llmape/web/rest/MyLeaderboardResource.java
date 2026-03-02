package uibk.llmape.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.service.leaderboard.LeaderboardDTO;
import uibk.llmape.service.leaderboard.LeaderboardEntryDTO;
import uibk.llmape.service.leaderboard.LeaderboardService;

/**
 * REST controller to get leaderboard information.
 */
@RestController
@RequestMapping("/api/myleaderboard")
@Transactional
public class MyLeaderboardResource {

	private static final Logger LOG = LoggerFactory.getLogger(MyLeaderboardResource.class);

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final LeaderboardService leaderboardService;

	public MyLeaderboardResource(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	/**
	 * {@code GET} : Get the current leaderboard based on a given category
	 *
	 * @param category the chosen category for the requested leaderboard.
	 * @return the {@link LeaderboardDTO>} which contains the leaderboard for the requested category.
	 */
	@GetMapping("getLeaderboard")
	public LeaderboardDTO getLeaderboard(@RequestParam(required = false) Category category) throws JsonProcessingException {
		LOG.debug("REST request to get leaderboard for category:{}", category);
		return leaderboardService.getLeaderboard(category);
	}
}
