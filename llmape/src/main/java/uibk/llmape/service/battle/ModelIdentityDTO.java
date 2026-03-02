package uibk.llmape.service.battle;

/**
 * A DTO representing the models' identities.
 */
public class ModelIdentityDTO {

	private static final long serialVersionUID = 1L;

	private String modelAIdentity;
	private String modelBIdentity;

	public ModelIdentityDTO(String modelAIdentity, String modelBIdentity) {
		this.modelAIdentity = modelAIdentity;
		this.modelBIdentity = modelBIdentity;
	}

	public String getModelAIdentity() {
		return modelAIdentity;
	}

	public void setModelAIdentity(String modelAIdentity) {
		this.modelAIdentity = modelAIdentity;
	}

	public String getModelBIdentity() {
		return modelBIdentity;
	}

	public void setModelBIdentity(String modelBIdentity) {
		this.modelBIdentity = modelBIdentity;
	}
}
