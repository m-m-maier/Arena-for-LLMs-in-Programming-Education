package uibk.llmape.service.llm;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class AnthropicProvider implements AIModelProvider {

	@Override
	public String getName() {
		return "anthropic";
	}

	@Override
	public ChatModel createChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = AnthropicChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}

	@Override
	public StreamingChatModel createStreamingChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = AnthropicStreamingChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}
}
