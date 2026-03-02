package uibk.llmape.service;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import uibk.llmape.domain.Model;
import uibk.llmape.repository.ModelRepository;
import uibk.llmape.repository.PromptRepository;
import uibk.llmape.service.llm.AIStreamTokenDTO;
import uibk.llmape.service.llm.LlmCacheService;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

@Service
public class PromptService {

	private static final String ENTITY_NAME = "prompt";

	private final PromptRepository promptRepository;
	private final ModelRepository modelRepository;
	private final LlmCacheService llmCacheService;

	@Autowired
	public PromptService(PromptRepository promptRepository, ModelRepository modelRepository, LlmCacheService llmCacheService) {
		this.promptRepository = promptRepository;
		this.modelRepository = modelRepository;
		this.llmCacheService = llmCacheService;
	}

	public Flux<AIStreamTokenDTO> getManyAIStreams(Long promptId, List<Long> modelIds) {
		var optionalPrompt = promptRepository.findById(promptId);
		var prompt = optionalPrompt.orElseThrow(() -> new IllegalArgumentException("Entity " + ENTITY_NAME + " not found"));

		var models = modelRepository.findAllById(modelIds);

		List<Flux<AIStreamTokenDTO>> streams = new LinkedList<>();
		for (var model : models) {
			streams.add(getFluxStreamForModel(model, prompt.getPromptText()));
		}

		return Flux.merge(streams);
	}

	private Flux<AIStreamTokenDTO> getFluxStreamForModel(Model model, String prompt) {
		var streamingChatModel = llmCacheService.getOrCreateStreamingChatModel(
			model.getProvider(),
			model.getModelName(),
			model.getApiKey(),
			model.getBaseUrl()
		);

		return Flux.create(sink -> {
			streamingChatModel.chat(
				prompt,
				new StreamingChatResponseHandler() {
					@Override
					public void onPartialResponse(String token) {
						sink.next(AIStreamTokenDTO.token(model.getId(), token));
					}

					@Override
					public void onCompleteResponse(ChatResponse chatResponse) {
						sink.next(AIStreamTokenDTO.complete(model.getId()));
						sink.complete();
					}

					@Override
					public void onError(Throwable throwable) {
						sink.error(throwable);
					}
				}
			);
		});
	}
}
