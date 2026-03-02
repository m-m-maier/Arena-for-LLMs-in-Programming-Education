package uibk.llmape.service.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class MistralProvider implements AIModelProvider {

	@Override
	public String getName() {
		return "mistral";
	}

	@Override
	public ChatModel createChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = MistralAiChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}

	@Override
	public StreamingChatModel createStreamingChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = MistralAiStreamingChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}
}
