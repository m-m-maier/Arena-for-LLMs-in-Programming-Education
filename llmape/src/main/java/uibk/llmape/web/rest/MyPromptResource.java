package uibk.llmape.web.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tech.jhipster.web.util.HeaderUtil;
import uibk.llmape.config.ApplicationProperties;
import uibk.llmape.config.Constants;
import uibk.llmape.domain.Battle;
import uibk.llmape.domain.Model;
import uibk.llmape.domain.Prompt;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.repository.BattleRepository;
import uibk.llmape.repository.ModelRepository;
import uibk.llmape.repository.PromptRepository;
import uibk.llmape.service.PromptService;
import uibk.llmape.service.battle.BattleService;
import uibk.llmape.service.battle.VoteOption;
import uibk.llmape.service.llm.AIResponseDTO;
import uibk.llmape.service.llm.AIStreamTokenDTO;
import uibk.llmape.service.llm.LlmCacheService;
import uibk.llmape.service.prompt.GeneratePromptDTO;
import uibk.llmape.service.prompt.GeneratePromptResponseDTO;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uibk.llmape.domain.Prompt}.
 */
@RestController
@RequestMapping("/api/myprompt")
@Transactional
public class MyPromptResource {

	private static final Logger LOG = LoggerFactory.getLogger(MyPromptResource.class);

	private static final String ENTITY_NAME = "prompt";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

    private final ApplicationProperties applicationProperties;
	private final PromptRepository promptRepository;
    private final ModelRepository modelRepository;
    private final BattleRepository battleRepository;
	private final LlmCacheService llmCacheService;
	private final BattleService battleService;
	private final PromptService promptService;

	public MyPromptResource(
        ApplicationProperties applicationProperties,
		PromptRepository promptRepository,
        ModelRepository modelRepository,
        BattleRepository battleRepository,
		LlmCacheService llmCacheService,
		BattleService battleService,
		PromptService promptService
	) {
        this.applicationProperties = applicationProperties;
		this.promptRepository = promptRepository;
        this.modelRepository = modelRepository;
        this.battleRepository = battleRepository;
		this.llmCacheService = llmCacheService;
		this.battleService = battleService;
		this.promptService = promptService;
	}

	/**
	 * {@code POST  /submitPrompt} : Create a new prompt, test if appropriate and initiate a battle.
	 *
	 * @param prompt the prompt to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the AIResponseDTO with isRejected and the validationResult, or with status {@code 400 (Bad Request)} if the prompt has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("submitPrompt")
	public ResponseEntity<AIResponseDTO> submitPrompt(@Valid @RequestBody Prompt prompt) throws URISyntaxException {
		LOG.debug("REST request to save Prompt : {}", prompt);
		if (prompt.getId() != null) {
			throw new BadRequestAlertException("A new prompt cannot already have an ID", ENTITY_NAME, "idexists");
		} else if (prompt.getPromptText().length() > Constants.MAX_LLM_INPUT_OUTPUT_CHARS) {
			throw new BadRequestAlertException("Prompt text limit exceeded", ENTITY_NAME, "texttoolong");
		}
		var promptCheck = checkIfPromptIsRejected(prompt);

        prompt.setIsRejected(promptCheck.isRejected);
        prompt = promptRepository.save(prompt);

		if (prompt.getIsRejected()) {
			return ResponseEntity.created(new URI("/api/prompts/" + prompt.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prompt.getId().toString()))
				.body(new AIResponseDTO(prompt.getIsRejected(), promptCheck.validationResult));
		}

		var battle = battleService.createBattle(prompt, null);
        var aiResponseDto = new AIResponseDTO(false, battle.getId());

		return ResponseEntity.created(new URI("/api/prompts/" + prompt.getId()))
			.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prompt.getId().toString()))
			.body(aiResponseDto);
	}

	/**
	 * {@code POST  /submitPrompt} : Create a new prompt for quick test llm page.
	 *
	 * @param prompt the prompt to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the id of the new prompt, or with status {@code 400 (Bad Request)} if there was a problem.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("submitQuickTestPrompt")
	public ResponseEntity<Long> submitQuickTestPrompt(@Valid @RequestBody Prompt prompt) throws URISyntaxException {
		LOG.debug("REST request to save Prompt : {}", prompt);
		if (prompt.getId() != null) {
			throw new BadRequestAlertException("A new prompt cannot already have an ID", ENTITY_NAME, "idexists");
		} else if (prompt.getPromptText().length() > Constants.MAX_LLM_INPUT_OUTPUT_CHARS) {
			throw new BadRequestAlertException("Prompt text limit exceeded", ENTITY_NAME, "texttoolong");
		}

		prompt = promptRepository.save(prompt);

		return ResponseEntity.created(new URI("/api/prompts/" + prompt.getId()))
			.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prompt.getId().toString()))
			.body(prompt.getId());
	}

	/**
	 * {@code GET  /streamAIResponses} : get a merged stream of two AI responses from models that are determined in battle with given battleId.
	 *
	 * @param battleId the id of the battle to know which AI models to stream from.
	 * @return the {@link AIStreamTokenDTO} as a stream (Flux<ServerSentEvent>)
	 */
	@GetMapping(value = "streamAIResponses", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<AIStreamTokenDTO>> streamAIResponses(@RequestParam Long battleId) {
		LOG.debug("REST request to stream AI responses for battle with id: {}", battleId);
        var optionalBattle = battleRepository.findById(battleId);
        var battle = optionalBattle.orElseThrow(() -> new BadRequestAlertException("Battle not found", "battle", "idnotfound"));

        var totalAnswer1 = new StringBuilder();
        var totalAnswer2 = new StringBuilder();
		var fluxStream = this.battleService.getAIStreams(battle, totalAnswer1, totalAnswer2);

        fluxStream.then(Mono.fromRunnable(() -> {
            battle.setModel1Answer(totalAnswer1.toString());
            battle.setModel2Answer(totalAnswer2.toString());
            battleRepository.save(battle);
        })).subscribeOn(Schedulers.boundedElastic()).subscribe();

		return fluxStream.map(token -> ServerSentEvent.builder(token).build());
	}

	/**
	 * {@code GET  /streamAIResponses} : get a merged stream of multiple AI responses from models with given ids
	 *
	 * @param promptId the id of the prompt.
	 * @param selectedModelIds the ids of the selected models to know which AI models to stream from.
	 * @return the {@link AIStreamTokenDTO} as a stream (Flux<ServerSentEvent>)
	 */
	@GetMapping(value = "streamManyAIResponses", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<AIStreamTokenDTO>> streamManyAIResponses(
		@RequestParam Long promptId,
		@RequestParam List<Long> selectedModelIds
	) {
		LOG.debug(
			"REST request to stream multiple AI responses for prompt with id: {} and models with ids: {}",
			promptId,
			selectedModelIds
		);
		if (selectedModelIds.size() < 1) {
			throw new BadRequestAlertException("No models selected to stream AI responses from", ENTITY_NAME, "nomodelsselected");
		}
		var fluxStream = this.promptService.getManyAIStreams(promptId, selectedModelIds);
		return fluxStream.map(token -> ServerSentEvent.builder(token).build());
	}

    /**
     * {@code POST  /generatePromptsAndVotes} : Generate a new prompt for the given category, test if appropriate, initiate a battle and let model vote.
     *
     * @param generatePromptDTO the dto that contains the
     * modelId (the id of the model to create a prompt with and vote) and
     * category (to create a prompt, battle and vote for)
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the GeneratePromptResponseDTO with wasSuccessful and detailedMessage, or with status {@code 400 (Bad Request)} if the model for given id was not found.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("generatePromptAndVote")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<GeneratePromptResponseDTO> generatePromptAndVote(@Valid @RequestBody @NotNull GeneratePromptDTO generatePromptDTO) throws URISyntaxException {
        LOG.debug("REST request to generate prompt, battle and vote with model: {} for category: {}", generatePromptDTO.getModelId(), generatePromptDTO.getCategory());

        var allActiveModels = modelRepository.findAllByActiveTrue();
        if(allActiveModels.size() < 3){
            throw new BadRequestAlertException("At least 3 active models needed to generate prompt and vote, currently active models in database: " + allActiveModels.size(), ENTITY_NAME, "notenoughmodels");
        }

        var model = modelRepository.findById(generatePromptDTO.getModelId()).orElseThrow(() ->
            new BadRequestAlertException("Model not found", "model", "idnotfound")
        );

        var currentSection = "Prompt generation";

        try{
            var generatedPrompt = generatePrompt(model, generatePromptDTO.getCategory());

            currentSection = "Prompt check";
            var promptCheck = checkIfPromptIsRejected(generatedPrompt);
            generatedPrompt.setIsRejected(promptCheck.isRejected);
            generatedPrompt = promptRepository.save(generatedPrompt);

            if(promptCheck.isRejected){
                return ResponseEntity.ok().body(new GeneratePromptResponseDTO(false, "Generation of prompt failed with model: " + model.getModelName() + " for category: " + generatePromptDTO.getCategory() + " Reason: Prompt was rejected by rejection model."));
            }

            currentSection = "Battle creation";
            var battle = battleService.createBattle(generatedPrompt, model);

            currentSection = "Streaming responses of two LLMs\nModel1: "  + battle.getModel1().getModelName() + "\nModel2: " + battle.getModel2().getModelName();
            var totalAnswer1 = new StringBuilder();
            var totalAnswer2 = new StringBuilder();
            battleService.getAIStreams(battle, totalAnswer1, totalAnswer2).then().block();
            battle.setModel1Answer(totalAnswer1.toString());
            battle.setModel2Answer(totalAnswer2.toString());
            battleRepository.save(battle);

            currentSection = "Vote generation";
            var voteSuccessful = generateVote(model, generatedPrompt, battle);

            if(!voteSuccessful){
                return ResponseEntity.ok().body(new GeneratePromptResponseDTO(false, "Generation of vote failed with model: " + model.getModelName() + " for category: " + generatePromptDTO.getCategory() + " Reason: Vote answer could not be identified as \"One\", \"Two\" or \"Tie\"."));
            }

        } catch (Exception e){
            return ResponseEntity.ok().body(new GeneratePromptResponseDTO(false, "Something went wrong. :/\nError occured during: " + currentSection));
        }
        return ResponseEntity.ok().body(new GeneratePromptResponseDTO(true, "Successful generation of prompt and vote with model: " + model.getModelName() + " for category: " + generatePromptDTO.getCategory()));
    }
    private PromptCheck checkIfPromptIsRejected(Prompt prompt){
        var rejectionModel = llmCacheService.getOrCreateChatModel(applicationProperties.getRejectionModel().getProvider(), applicationProperties.getRejectionModel().getModelName(), applicationProperties.getRejectionModel().getToken(), applicationProperties.getRejectionModel().getBaseurl());

        var categorySpecificPromptPart = "";
        switch (prompt.getCategory()) {
            case HINT_GENERATION -> categorySpecificPromptPart =
                "hint generation, which means there is a request for help in any way for any kind of programming problem or exercise. ";
            case EXERCISE_GENERATION -> categorySpecificPromptPart =
                "exercise generation, which means there is a request to generate or create an exercise or ideas for exercises. ";
            case CODE_ASSESSMENT -> categorySpecificPromptPart =
                "code assessment, which means there is a request to assess, grade, or give feedback for some given code (there must be code contained in the prompt!). ";
        }

        var generalPromptStart =
            "Answer with the single word 'Yes' if the given prompt fulfills the given restrictions between the '<Restrictions>' tags. Do not answer or comment on the prompt itself. Don't be too strict with the restrictions, also answer just with 'Yes' if the prompt somehow relates to the given topic in any way. If the restrictions are not fulfilled in any way, answer with 'Rejected because:' and then explain why it doesn't fulfill the given restrictions. <Restrictions>: The following prompt after the ':' must be somehow appropriate regarding ";
        var generalPromptMiddle = "The prompt should somehow be related to programming. <Restrictions> The prompt is as follows: ";

        var validationPrompt = generalPromptStart + categorySpecificPromptPart + generalPromptMiddle + prompt.getPromptText();
        var validationResult = rejectionModel.chat(validationPrompt);

        var isRejected = !validationResult.equalsIgnoreCase("Yes");

        return new PromptCheck(isRejected, validationResult);
    }

    private Prompt generatePrompt(Model model, Category category){
        var promptGenerationModel = llmCacheService.getOrCreateChatModel(model.getProvider(), model.getModelName(), model.getApiKey(), model.getBaseUrl());

        var categorySpecificPromptPart = "";
        switch (category) {
            case HINT_GENERATION -> categorySpecificPromptPart =
                "ask for help for a programming exercise, task, bug detection, or bugfix. Choose the exercise, task or bug yourself. ";
            case EXERCISE_GENERATION -> categorySpecificPromptPart =
                "ask for generating an exercise or assignment to practice some specific programming topic in a given programming language. Choose the topic and programming language yourself.";
            case CODE_ASSESSMENT -> categorySpecificPromptPart =
                "ask for an assessment of some given code to receive help with grading the solution. Provide the code and exercise it belongs to yourself.";
        }

        var generalPromptStart =
            "Provide a single prompt that can be used to prompt other LLMs. Your answer should only contain the prompt including possibly necessary code to answer your prompt, nothing else. The prompt should ";

        var generationPrompt = generalPromptStart + categorySpecificPromptPart;

        var generatedPrompt = promptGenerationModel.chat(generationPrompt);

        var prompt = new Prompt();
        prompt.setPromptText(generatedPrompt);
        prompt.setCategory(category);
        prompt.setIsFromPublicPage(false);
        prompt.setTimestamp(Instant.now());
        prompt.setGenerationModelId(model.getId());
        prompt.setSessionId("generator-session");

        return prompt;

    }

    private boolean generateVote(Model model, Prompt prompt, Battle battle){
        var voteGenerationModel = llmCacheService.getOrCreateChatModel(model.getProvider(), model.getModelName(), model.getApiKey(), model.getBaseUrl());
        var stringBuilder = new StringBuilder();

        stringBuilder.append("In the following, there are two different responses regarding your prompt. The prompt is between [START_PROMPT] and [END_PROMPT]. Response 1 is between [START_RESPONSE_1] and [END_RESPONSE_1]. Response 2 is between [START_RESPONSE_2] and [END_RESPONSE_2].\n");
        stringBuilder.append("You should only answer with a single word and nothing more. The answer should be \"One\" if response 1 is better regarding your prompt. The answer should be \"Two\" if response 2 is better regarding your prompt. The answer should be \"Tie\" if response 1 and 2 are the same or equally good answers to your prompt.\n");

        stringBuilder.append("[START_PROMPT] ");
        if(prompt.getCategory() == Category.HINT_GENERATION){
            stringBuilder.append("You are an excellent tutor. An excellent tutor is a guide and an educator. Your main goal is to teach students problem-solving skills while they work on a programming exercise. An excellent tutor never under any circumstances responds with code, pseudocode, or implementations of concrete functionalities. An excellent tutor never under any circumstances tells instructions that contain concrete steps and implementation details. Instead, he provides a single subtle clue, a counter-question, or best practice to move the student’s attention to an aspect of his problem or task so they can find a solution on their own. An excellent tutor does not guess, so if you don’t know something, say \"Sorry, I don’t know\" and tell the student to ask a human tutor. The students prompt is: ");
        }
        stringBuilder.append(prompt.getPromptText());
        stringBuilder.append(" [END_PROMPT]\n");

        stringBuilder.append("[START_RESPONSE_1] ");
        stringBuilder.append(battle.getModel1Answer());
        stringBuilder.append(" [END_RESPONSE_1]\n");

        stringBuilder.append("[START_RESPONSE_2] ");
        stringBuilder.append(battle.getModel2Answer());
        stringBuilder.append(" [END_RESPONSE_2]");

        var voteGenerationPrompt = stringBuilder.toString();
        var voteAnswer = voteGenerationModel.chat(voteGenerationPrompt);

        if(voteAnswer.equalsIgnoreCase("One")){
            battleService.submitVote(battle, VoteOption.MODEL_A_BETTER);
            return true;
        } else if(voteAnswer.equalsIgnoreCase("Two")){
            battleService.submitVote(battle, VoteOption.MODEL_B_BETTER);
            return true;
        } else if(voteAnswer.equalsIgnoreCase("Tie")){
            battleService.submitVote(battle, VoteOption.TIE);
            return true;
        }

        return false;
    }

    private record PromptCheck(boolean isRejected, String validationResult){}

}
