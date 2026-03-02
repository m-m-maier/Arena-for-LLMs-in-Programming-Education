package uibk.llmape.service.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class OpenAiProvider implements AIModelProvider {

	@Override
	public String getName() {
		return "openai";
	}

	@Override
	public ChatModel createChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = OpenAiChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}

	@Override
	public StreamingChatModel createStreamingChatModel(String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		var builder = OpenAiStreamingChatModel.builder().modelName(modelName).apiKey(apiKey);

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder.build();
	}
}
