package uibk.llmape.service.dto;

/**
 * A DTO representing the model with just its id and modelName.
 */
public class ModelDTO {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String modelName;

	public ModelDTO(Long id, String modelName) {
		this.id = id;
		this.modelName = modelName;
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
}
