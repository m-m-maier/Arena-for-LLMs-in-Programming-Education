package uibk.llmape.service.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class GoogleProvider implements AIModelProvider {

	@Override
	public String getName() {
		return "google";
	}

	@Override
	public ChatModel createChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = GoogleAiGeminiChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}

	@Override
	public StreamingChatModel createStreamingChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = GoogleAiGeminiStreamingChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}
}
