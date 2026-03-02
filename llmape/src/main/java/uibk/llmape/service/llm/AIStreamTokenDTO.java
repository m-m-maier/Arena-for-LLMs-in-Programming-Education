package uibk.llmape.service.llm;

/**
 * A DTO representing AI responses that are presented to the user.
 */
public class AIStreamTokenDTO {

	private static final long serialVersionUID = 1L;

	private Long source;
	private String token;

	private boolean isComplete = true;

	public AIStreamTokenDTO(Long source, String token, boolean isComplete) {
		this.source = source;
		this.token = token;
		this.isComplete = isComplete;
	}

	public static AIStreamTokenDTO token(Long source, String token) {
		return new AIStreamTokenDTO(source, token, false);
	}

	public static AIStreamTokenDTO complete(Long source) {
		return new AIStreamTokenDTO(source, "", true);
	}

	public Long getSource() {
		return source;
	}

	public void setSource(Long source) {
		this.source = source;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean complete) {
		isComplete = complete;
	}
}
