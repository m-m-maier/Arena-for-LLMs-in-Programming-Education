package uibk.llmape.service.battle;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import uibk.llmape.config.Constants;
import uibk.llmape.domain.Battle;
import uibk.llmape.domain.Model;
import uibk.llmape.domain.Prompt;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.repository.BattleRepository;
import uibk.llmape.repository.ModelRepository;
import uibk.llmape.service.llm.AIStreamTokenDTO;
import uibk.llmape.service.llm.LlmCacheService;

@Service
public class BattleService {

	private static final String ENTITY_NAME = "battle";

	private final BattleRepository battleRepository;
	private final ModelRepository modelRepository;
	private final LlmCacheService llmCacheService;

    @Autowired
	public BattleService(BattleRepository battleRepository, ModelRepository modelRepository, LlmCacheService llmCacheService) {
		this.battleRepository = battleRepository;
		this.modelRepository = modelRepository;
		this.llmCacheService = llmCacheService;
	}

	public Battle createBattle(Prompt prompt, Model excludeModel) {
		var selectedModels = getModelPairWithVoteAwareLikelihood(prompt.getCategory(), excludeModel);
		var model1 = selectedModels.getFirst();
		var model2 = selectedModels.getSecond();
		var battle = new Battle();
		battle.setPrompt(prompt);
		battle.setModel1(model1);
		battle.setModel2(model2);
		battle = battleRepository.save(battle);

		return battle;
	}

	public Flux<AIStreamTokenDTO> getAIStreams(Battle battle, StringBuilder totalAnswer1, StringBuilder totalAnswer2) {

		var prompt = battle.getPrompt().getPromptText();

        if(battle.getPrompt().getCategory() == Category.HINT_GENERATION){
            String hintGenerationPreText = "You are an excellent tutor. An excellent tutor is a guide and an educator. Your main goal is to teach students problem-solving skills while they work on a programming exercise. An excellent tutor never under any circumstances responds with code, pseudocode, or implementations of concrete functionalities. An excellent tutor never under any circumstances tells instructions that contain concrete steps and implementation details. Instead, he provides a single subtle clue, a counter-question, or best practice to move the student’s attention to an aspect of his problem or task so they can find a solution on their own. An excellent tutor does not guess, so if you don’t know something, say \"Sorry, I don’t know\" and tell the student to ask a human tutor. The students prompt is: ";
            prompt = hintGenerationPreText + prompt;
        }

		var stream1 = getFluxStreamForModel(totalAnswer1, prompt, battle.getModel1(), 1L);
		var stream2 = getFluxStreamForModel(totalAnswer2, prompt, battle.getModel2(), 2L);

		return Flux.merge(stream1, stream2);
	}

	private Flux<AIStreamTokenDTO> getFluxStreamForModel(StringBuilder totalAnswer, String prompt, Model model, long source) {
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
						totalAnswer.append(token);
						sink.next(AIStreamTokenDTO.token(source, token));
					}

					@Override
					public void onCompleteResponse(ChatResponse chatResponse) {
						if (totalAnswer.length() > Constants.MAX_LLM_INPUT_OUTPUT_CHARS) {
							totalAnswer.setLength(Constants.MAX_LLM_INPUT_OUTPUT_CHARS);
						}
						sink.next(AIStreamTokenDTO.complete(source));
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

	public Battle submitVote(Battle battle, VoteOption voteOption) {
		if (battle.getVoteTimestamp() != null) {
			throw new IllegalArgumentException("Vote not allowed, vote already submitted!");
		}

		switch (voteOption) {
			case MODEL_A_BETTER -> battle.setWinnerModel(battle.getModel1());
			case MODEL_B_BETTER -> battle.setWinnerModel(battle.getModel2());
		}

		battle.setVoteTimestamp(Instant.now());

		return battleRepository.save(battle);
	}

	private Pair<Model, Model> getModelPairWithVoteAwareLikelihood(Category category, Model excludeModel) {
		var allActiveModels = this.modelRepository.findAllByActiveTrue();
		var battlePairCounts = this.battleRepository.findVotedBattlePairCounts(category);

        if(excludeModel != null){
            allActiveModels.remove(excludeModel);
            battlePairCounts.removeIf(bpc -> bpc.getModelId1().equals(excludeModel.getId()) || bpc.getModelId2().equals(excludeModel.getId()));
        }

		Map<Pair<Long, Long>, Long> pairCountMap = new HashMap<>();
		for (var pairCount : battlePairCounts) {
			pairCountMap.put(Pair.of(pairCount.getModelId1(), pairCount.getModelId2()), pairCount.getCount());
		}

		var totalBattles = 0;
		for (var pair : battlePairCounts) {
			totalBattles += pair.getCount();
		}

		List<Pair<Model, Model>> allPairs = new ArrayList<>();
		for (int i = 0; i < allActiveModels.size(); i++) {
			for (int j = i + 1; j < allActiveModels.size(); j++) { //ensure to not have a pair of two same models
				allPairs.add(Pair.of(allActiveModels.get(i), allActiveModels.get(j)));
			}
		}

		var p = 5; //parameter to tune sharpness (the higher the more influenced is the likelihood towards the less often picked pairs)
		double weightSum = 0.0;

		var weightedPairs = new LinkedList<PairWithWeight>();

		for (var pair : allPairs) {
			Long id1 = pair.getFirst().getId();
			Long id2 = pair.getSecond().getId();
			double frequency = 0.0;

			if (totalBattles == 0) {
				frequency = (double) 1 / (double) allPairs.size();
			} else {
				long count = pairCountMap.getOrDefault(Pair.of(Math.min(id1, id2), Math.max(id1, id2)), 0L);
				frequency = (double) count / (double) totalBattles;
			}

			double weight = Math.pow(1.0 - frequency, p);
			weightedPairs.add(new PairWithWeight(pair, weight));
			weightSum += weight;
		}

		double rand = Math.random() * weightSum;
		double cumulative = 0.0;

		for (PairWithWeight weightedPair : weightedPairs) {
			cumulative += weightedPair.weight;
			if (rand <= cumulative) {
				return getShuffledPair(weightedPair.pair);
			}
		}

		return getShuffledPair(weightedPairs.get(weightedPairs.size() - 1).pair); // fallback
	}

	private record PairWithWeight(Pair<Model, Model> pair, double weight) {}

	private Pair<Model, Model> getShuffledPair(Pair<Model, Model> pair) {
		double rand = Math.random();
		if (rand >= 0.5) {
			return Pair.of(pair.getSecond(), pair.getFirst());
		}

		return pair;
	}
}
