package uibk.llmape.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uibk.llmape.domain.PromptAsserts.*;
import static uibk.llmape.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uibk.llmape.IntegrationTest;
import uibk.llmape.domain.Prompt;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.repository.PromptRepository;
import uibk.llmape.security.AuthoritiesConstants;

/**
 * Integration tests for the {@link PromptResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class PromptResourceIT {

	private static final String DEFAULT_PROMPT_TEXT = "AAAAAAAAAA";
	private static final String UPDATED_PROMPT_TEXT = "BBBBBBBBBB";

	private static final Category DEFAULT_CATEGORY = Category.HINT_GENERATION;
	private static final Category UPDATED_CATEGORY = Category.EXERCISE_GENERATION;

	private static final Boolean DEFAULT_IS_REJECTED = false;
	private static final Boolean UPDATED_IS_REJECTED = true;

	private static final Boolean DEFAULT_IS_FROM_PUBLIC_PAGE = false;
	private static final Boolean UPDATED_IS_FROM_PUBLIC_PAGE = true;

	private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
	private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

	private static final String DEFAULT_SESSION_ID = "AAAAAAAAAA";
	private static final String UPDATED_SESSION_ID = "BBBBBBBBBB";

	private static final Long DEFAULT_GENERATION_MODEL_ID = 1L;
	private static final Long UPDATED_GENERATION_MODEL_ID = 2L;

	private static final String ENTITY_API_URL = "/api/prompts";
	private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

	private static Random random = new Random();
	private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	@Autowired
	private ObjectMapper om;

	@Autowired
	private PromptRepository promptRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private MockMvc restPromptMockMvc;

	private Prompt prompt;

	private Prompt insertedPrompt;

	/**
	 * Create an entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Prompt createEntity() {
		return new Prompt()
			.promptText(DEFAULT_PROMPT_TEXT)
			.category(DEFAULT_CATEGORY)
			.isRejected(DEFAULT_IS_REJECTED)
			.isFromPublicPage(DEFAULT_IS_FROM_PUBLIC_PAGE)
			.timestamp(DEFAULT_TIMESTAMP)
			.sessionId(DEFAULT_SESSION_ID)
			.generationModelId(DEFAULT_GENERATION_MODEL_ID);
	}

	/**
	 * Create an updated entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Prompt createUpdatedEntity() {
		return new Prompt()
			.promptText(UPDATED_PROMPT_TEXT)
			.category(UPDATED_CATEGORY)
			.isRejected(UPDATED_IS_REJECTED)
			.isFromPublicPage(UPDATED_IS_FROM_PUBLIC_PAGE)
			.timestamp(UPDATED_TIMESTAMP)
			.sessionId(UPDATED_SESSION_ID)
			.generationModelId(UPDATED_GENERATION_MODEL_ID);
	}

	@BeforeEach
	public void initTest() {
		prompt = createEntity();
	}

	@AfterEach
	public void cleanup() {
		if (insertedPrompt != null) {
			promptRepository.delete(insertedPrompt);
			insertedPrompt = null;
		}
	}

	@Test
	@Transactional
	void createPrompt() throws Exception {
		long databaseSizeBeforeCreate = getRepositoryCount();
		// Create the Prompt
		var returnedPrompt = om.readValue(
			restPromptMockMvc
				.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString(),
			Prompt.class
		);

		// Validate the Prompt in the database
		assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
		assertPromptUpdatableFieldsEquals(returnedPrompt, getPersistedPrompt(returnedPrompt));

		insertedPrompt = returnedPrompt;
	}

	@Test
	@Transactional
	void createPromptWithExistingId() throws Exception {
		// Create the Prompt with an existing ID
		prompt.setId(1L);

		long databaseSizeBeforeCreate = getRepositoryCount();

		// An entity with an existing ID cannot be created, so this API call must fail
		restPromptMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeCreate);
	}

	@Test
	@Transactional
	void checkPromptTextIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		prompt.setPromptText(null);

		// Create the Prompt, which fails.

		restPromptMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkIsRejectedIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		prompt.setIsRejected(null);

		// Create the Prompt, which fails.

		restPromptMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkIsFromPublicPageIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		prompt.setIsFromPublicPage(null);

		// Create the Prompt, which fails.

		restPromptMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkTimestampIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		prompt.setTimestamp(null);

		// Create the Prompt, which fails.

		restPromptMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkSessionIdIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		prompt.setSessionId(null);

		// Create the Prompt, which fails.

		restPromptMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void getAllPrompts() throws Exception {
		// Initialize the database
		insertedPrompt = promptRepository.saveAndFlush(prompt);

		// Get all the promptList
		restPromptMockMvc
			.perform(get(ENTITY_API_URL + "?sort=id,desc"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.[*].id").value(hasItem(prompt.getId().intValue())))
			.andExpect(jsonPath("$.[*].promptText").value(hasItem(DEFAULT_PROMPT_TEXT)))
			.andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
			.andExpect(jsonPath("$.[*].isRejected").value(hasItem(DEFAULT_IS_REJECTED)))
			.andExpect(jsonPath("$.[*].isFromPublicPage").value(hasItem(DEFAULT_IS_FROM_PUBLIC_PAGE)))
			.andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())))
			.andExpect(jsonPath("$.[*].sessionId").value(hasItem(DEFAULT_SESSION_ID)))
			.andExpect(jsonPath("$.[*].generationModelId").value(hasItem(DEFAULT_GENERATION_MODEL_ID.intValue())));
	}

	@Test
	@Transactional
	void getPrompt() throws Exception {
		// Initialize the database
		insertedPrompt = promptRepository.saveAndFlush(prompt);

		// Get the prompt
		restPromptMockMvc
			.perform(get(ENTITY_API_URL_ID, prompt.getId()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.id").value(prompt.getId().intValue()))
			.andExpect(jsonPath("$.promptText").value(DEFAULT_PROMPT_TEXT))
			.andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
			.andExpect(jsonPath("$.isRejected").value(DEFAULT_IS_REJECTED))
			.andExpect(jsonPath("$.isFromPublicPage").value(DEFAULT_IS_FROM_PUBLIC_PAGE))
			.andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()))
			.andExpect(jsonPath("$.sessionId").value(DEFAULT_SESSION_ID))
			.andExpect(jsonPath("$.generationModelId").value(DEFAULT_GENERATION_MODEL_ID.intValue()));
	}

	@Test
	@Transactional
	void getNonExistingPrompt() throws Exception {
		// Get the prompt
		restPromptMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	void putExistingPrompt() throws Exception {
		// Initialize the database
		insertedPrompt = promptRepository.saveAndFlush(prompt);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the prompt
		Prompt updatedPrompt = promptRepository.findById(prompt.getId()).orElseThrow();
		// Disconnect from session so that the updates on updatedPrompt are not directly saved in db
		em.detach(updatedPrompt);
		updatedPrompt
			.promptText(UPDATED_PROMPT_TEXT)
			.category(UPDATED_CATEGORY)
			.isRejected(UPDATED_IS_REJECTED)
			.isFromPublicPage(UPDATED_IS_FROM_PUBLIC_PAGE)
			.timestamp(UPDATED_TIMESTAMP)
			.sessionId(UPDATED_SESSION_ID)
			.generationModelId(UPDATED_GENERATION_MODEL_ID);

		restPromptMockMvc
			.perform(
				put(ENTITY_API_URL_ID, updatedPrompt.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(updatedPrompt))
			)
			.andExpect(status().isOk());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertPersistedPromptToMatchAllProperties(updatedPrompt);
	}

	@Test
	@Transactional
	void putNonExistingPrompt() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		prompt.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restPromptMockMvc
			.perform(put(ENTITY_API_URL_ID, prompt.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isBadRequest());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithIdMismatchPrompt() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		prompt.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restPromptMockMvc
			.perform(
				put(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(prompt))
			)
			.andExpect(status().isBadRequest());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithMissingIdPathParamPrompt() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		prompt.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restPromptMockMvc
			.perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void partialUpdatePromptWithPatch() throws Exception {
		// Initialize the database
		insertedPrompt = promptRepository.saveAndFlush(prompt);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the prompt using partial update
		Prompt partialUpdatedPrompt = new Prompt();
		partialUpdatedPrompt.setId(prompt.getId());

		partialUpdatedPrompt
			.category(UPDATED_CATEGORY)
			.isRejected(UPDATED_IS_REJECTED)
			.isFromPublicPage(UPDATED_IS_FROM_PUBLIC_PAGE)
			.generationModelId(UPDATED_GENERATION_MODEL_ID);

		restPromptMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedPrompt.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedPrompt))
			)
			.andExpect(status().isOk());

		// Validate the Prompt in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertPromptUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPrompt, prompt), getPersistedPrompt(prompt));
	}

	@Test
	@Transactional
	void fullUpdatePromptWithPatch() throws Exception {
		// Initialize the database
		insertedPrompt = promptRepository.saveAndFlush(prompt);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the prompt using partial update
		Prompt partialUpdatedPrompt = new Prompt();
		partialUpdatedPrompt.setId(prompt.getId());

		partialUpdatedPrompt
			.promptText(UPDATED_PROMPT_TEXT)
			.category(UPDATED_CATEGORY)
			.isRejected(UPDATED_IS_REJECTED)
			.isFromPublicPage(UPDATED_IS_FROM_PUBLIC_PAGE)
			.timestamp(UPDATED_TIMESTAMP)
			.sessionId(UPDATED_SESSION_ID)
			.generationModelId(UPDATED_GENERATION_MODEL_ID);

		restPromptMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedPrompt.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedPrompt))
			)
			.andExpect(status().isOk());

		// Validate the Prompt in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertPromptUpdatableFieldsEquals(partialUpdatedPrompt, getPersistedPrompt(partialUpdatedPrompt));
	}

	@Test
	@Transactional
	void patchNonExistingPrompt() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		prompt.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restPromptMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, prompt.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prompt))
			)
			.andExpect(status().isBadRequest());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithIdMismatchPrompt() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		prompt.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restPromptMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(prompt))
			)
			.andExpect(status().isBadRequest());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithMissingIdPathParamPrompt() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		prompt.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restPromptMockMvc
			.perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prompt)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the Prompt in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void deletePrompt() throws Exception {
		// Initialize the database
		insertedPrompt = promptRepository.saveAndFlush(prompt);

		long databaseSizeBeforeDelete = getRepositoryCount();

		// Delete the prompt
		restPromptMockMvc
			.perform(delete(ENTITY_API_URL_ID, prompt.getId()).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		// Validate the database contains one less item
		assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
	}

	protected long getRepositoryCount() {
		return promptRepository.count();
	}

	protected void assertIncrementedRepositoryCount(long countBefore) {
		assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
	}

	protected void assertDecrementedRepositoryCount(long countBefore) {
		assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
	}

	protected void assertSameRepositoryCount(long countBefore) {
		assertThat(countBefore).isEqualTo(getRepositoryCount());
	}

	protected Prompt getPersistedPrompt(Prompt prompt) {
		return promptRepository.findById(prompt.getId()).orElseThrow();
	}

	protected void assertPersistedPromptToMatchAllProperties(Prompt expectedPrompt) {
		assertPromptAllPropertiesEquals(expectedPrompt, getPersistedPrompt(expectedPrompt));
	}

	protected void assertPersistedPromptToMatchUpdatableProperties(Prompt expectedPrompt) {
		assertPromptAllUpdatablePropertiesEquals(expectedPrompt, getPersistedPrompt(expectedPrompt));
	}
}
