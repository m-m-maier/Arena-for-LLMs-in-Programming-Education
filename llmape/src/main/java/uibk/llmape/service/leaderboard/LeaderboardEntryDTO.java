package uibk.llmape.service.leaderboard;

/**
 * A DTO representing a leaderboard entry consisting of model name and score.
 */
public class LeaderboardEntryDTO {

	private static final long serialVersionUID = 1L;

	private Long id;
	private float score;

	public LeaderboardEntryDTO(Long id, float score) {
		this.id = id;
		this.score = score;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}
}
