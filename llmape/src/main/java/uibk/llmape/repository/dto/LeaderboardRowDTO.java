package uibk.llmape.repository.dto;

/**
 * A DTO representing a row of the leaderboard.
 */
public class LeaderboardRowDTO {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String modelName;
	private float score;
	private Long numberOfVotes;
	private Long numberOfTies;
	private String organization;
	private String license;

	public LeaderboardRowDTO(
		Long id,
		String modelName,
		float score,
		Long numberOfVotes,
		Long numberOfTies,
		String organization,
		String license
	) {
		this.id = id;
		this.modelName = modelName;
		this.score = score;
		this.numberOfVotes = numberOfVotes;
		this.numberOfTies = numberOfTies;
		this.organization = organization;
		this.license = license;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public Long getNumberOfVotes() {
		return numberOfVotes;
	}

	public void setNumberOfVotes(Long numberOfVotes) {
		this.numberOfVotes = numberOfVotes;
	}

	public Long getNumberOfTies() {
		return numberOfTies;
	}

	public void setNumberOfTies(Long numberOfTies) {
		this.numberOfTies = numberOfTies;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}
}
