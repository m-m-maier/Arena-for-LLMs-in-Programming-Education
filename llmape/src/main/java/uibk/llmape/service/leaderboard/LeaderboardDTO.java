package uibk.llmape.service.leaderboard;

import java.time.Instant;
import java.util.List;
import uibk.llmape.repository.dto.LeaderboardRowDTO;

/**
 * A DTO representing the leaderboard.
 */
public class LeaderboardDTO {

	private static final long serialVersionUID = 1L;

	private List<LeaderboardRowDTO> rows;
	private Instant timestamp;

	public LeaderboardDTO(List<LeaderboardRowDTO> rows, Instant timestamp) {
		this.rows = rows;
		this.timestamp = timestamp;
	}

	public List<LeaderboardRowDTO> getRows() {
		return rows;
	}

	public void setRows(List<LeaderboardRowDTO> rows) {
		this.rows = rows;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
}
