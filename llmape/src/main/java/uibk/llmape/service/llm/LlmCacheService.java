package uibk.llmape.service.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class LlmCacheService {

	private final Map<ModelKey, ChatModel> chatModelCache = new ConcurrentHashMap<>();
	private final Map<ModelKey, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();

	private final Map<String, AIModelProvider> providers;

	@Autowired
	public LlmCacheService(List<AIModelProvider> providerList) {
		this.providers = providerList.stream().collect(Collectors.toMap(AIModelProvider::getName, Function.identity()));
	}

	public ChatModel getOrCreateChatModel(String providerName, String modelName, @Nullable String apiKey, @Nullable String baseUrl) {
		ModelKey key = new ModelKey(providerName, modelName);

		return chatModelCache.computeIfAbsent(key, k -> {
			AIModelProvider provider = providers.get(providerName.toLowerCase());
			if (provider == null) {
				throw new IllegalArgumentException("Unknown provider: " + providerName);
			}
			return provider.createChatModel(modelName, apiKey, baseUrl);
		});
	}

	public StreamingChatModel getOrCreateStreamingChatModel(
		String providerName,
		String modelName,
		@Nullable String apiKey,
		@Nullable String baseUrl
	) {
		ModelKey key = new ModelKey(providerName, modelName);

		return streamingChatModelCache.computeIfAbsent(key, k -> {
			AIModelProvider provider = providers.get(providerName.toLowerCase());
			if (provider == null) {
				throw new IllegalArgumentException("Unknown provider: " + providerName);
			}
			return provider.createStreamingChatModel(modelName, apiKey, baseUrl);
		});
	}
}
