package uibk.llmape.service.prompt;

import uibk.llmape.domain.enumeration.Category;

/**
 * A DTO containing the request data for generating a prompt with vote.
 */
public class GeneratePromptDTO {

	private static final long serialVersionUID = 1L;

	private long modelId;
	private Category category;

    public GeneratePromptDTO(long modelId, Category category) {
        this.modelId = modelId;
        this.category = category;
    }

    public long getModelId() {
        return modelId;
    }

    public void setModelId(long modelId) {
        this.modelId = modelId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
