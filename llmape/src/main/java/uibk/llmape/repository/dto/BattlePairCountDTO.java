package uibk.llmape.repository.dto;

/**
 * A DTO representing a vote for a given battle.
 */
public class BattlePairCountDTO {

	private static final long serialVersionUID = 1L;

	private final Long modelId1;
	private final Long modelId2;
	private final Long count;

	public BattlePairCountDTO(Long idA, Long idB, Long count) {
		if (idA < idB) {
			this.modelId1 = idA;
			this.modelId2 = idB;
		} else {
			this.modelId1 = idB;
			this.modelId2 = idA;
		}
		this.count = count;
	}

	public Long getModelId1() {
		return modelId1;
	}

	public Long getModelId2() {
		return modelId2;
	}

	public Long getCount() {
		return count;
	}
}
