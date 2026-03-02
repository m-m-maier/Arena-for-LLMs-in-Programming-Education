package uibk.llmape.service.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.lang.Nullable;

public interface AIModelProvider {
	String getName();

	ChatModel createChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl);
	StreamingChatModel createStreamingChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl);
}
