package uibk.llmape.service.llm;

/**
 * A DTO representing AI responses that are presented to the user.
 */
public class AIResponseDTO {

	private static final long serialVersionUID = 1L;

	private boolean isRejected;
	private long battleId;
	private String rejectionReason;

	public AIResponseDTO(boolean isRejected, long battleId) {
		this.isRejected = isRejected;
		this.battleId = battleId;
	}

	public AIResponseDTO(boolean isRejected, String rejectionReason) {
		this.isRejected = isRejected;
		this.rejectionReason = rejectionReason;
	}

	public boolean isRejected() {
		return isRejected;
	}

	public void setRejected(boolean rejected) {
		isRejected = rejected;
	}

	public long getBattleId() {
		return battleId;
	}

	public void setBattleId(long battleId) {
		this.battleId = battleId;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}
