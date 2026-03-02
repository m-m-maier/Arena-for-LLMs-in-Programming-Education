package uibk.llmape.service.prompt;

/**
 * A DTO containing the response data for generating a prompt with vote.
 */
public class GeneratePromptResponseDTO {

	private static final long serialVersionUID = 1L;

	private boolean wasSuccessful;
	private String detailedMessage;

    public GeneratePromptResponseDTO(boolean wasSuccessful, String detailedMessage) {
        this.wasSuccessful = wasSuccessful;
        this.detailedMessage = detailedMessage;
    }

    public boolean getWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }
}
