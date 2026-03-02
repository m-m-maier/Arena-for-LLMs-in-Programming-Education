package uibk.llmape.service.battle;

/**
 * A DTO representing a vote for a given battle.
 */
public class VoteDTO {

	private static final long serialVersionUID = 1L;

	private long battleId;
	private VoteOption voteOption;

	public VoteDTO(long battleId, VoteOption voteOption) {
		this.battleId = battleId;
		this.voteOption = voteOption;
	}

	public long getBattleId() {
		return battleId;
	}

	public void setBattleId(long battleId) {
		this.battleId = battleId;
	}

	public VoteOption getVoteOption() {
		return voteOption;
	}

	public void setVoteOption(VoteOption voteOption) {
		this.voteOption = voteOption;
	}
}
