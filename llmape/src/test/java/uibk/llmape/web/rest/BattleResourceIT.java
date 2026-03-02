package uibk.llmape.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uibk.llmape.domain.BattleAsserts.*;
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
import uibk.llmape.domain.Battle;
import uibk.llmape.repository.BattleRepository;
import uibk.llmape.security.AuthoritiesConstants;

/**
 * Integration tests for the {@link BattleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class BattleResourceIT {

	private static final String DEFAULT_MODEL_1_ANSWER = "AAAAAAAAAA";
	private static final String UPDATED_MODEL_1_ANSWER = "BBBBBBBBBB";

	private static final String DEFAULT_MODEL_2_ANSWER = "AAAAAAAAAA";
	private static final String UPDATED_MODEL_2_ANSWER = "BBBBBBBBBB";

	private static final Instant DEFAULT_VOTE_TIMESTAMP = Instant.ofEpochMilli(0L);
	private static final Instant UPDATED_VOTE_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

	private static final String ENTITY_API_URL = "/api/battles";
	private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

	private static Random random = new Random();
	private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	@Autowired
	private ObjectMapper om;

	@Autowired
	private BattleRepository battleRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private MockMvc restBattleMockMvc;

	private Battle battle;

	private Battle insertedBattle;

	/**
	 * Create an entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Battle createEntity() {
		return new Battle().model1Answer(DEFAULT_MODEL_1_ANSWER).model2Answer(DEFAULT_MODEL_2_ANSWER).voteTimestamp(DEFAULT_VOTE_TIMESTAMP);
	}

	/**
	 * Create an updated entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Battle createUpdatedEntity() {
		return new Battle().model1Answer(UPDATED_MODEL_1_ANSWER).model2Answer(UPDATED_MODEL_2_ANSWER).voteTimestamp(UPDATED_VOTE_TIMESTAMP);
	}

	@BeforeEach
	public void initTest() {
		battle = createEntity();
	}

	@AfterEach
	public void cleanup() {
		if (insertedBattle != null) {
			battleRepository.delete(insertedBattle);
			insertedBattle = null;
		}
	}

	@Test
	@Transactional
	void createBattle() throws Exception {
		long databaseSizeBeforeCreate = getRepositoryCount();
		// Create the Battle
		var returnedBattle = om.readValue(
			restBattleMockMvc
				.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(battle)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString(),
			Battle.class
		);

		// Validate the Battle in the database
		assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
		assertBattleUpdatableFieldsEquals(returnedBattle, getPersistedBattle(returnedBattle));

		insertedBattle = returnedBattle;
	}

	@Test
	@Transactional
	void createBattleWithExistingId() throws Exception {
		// Create the Battle with an existing ID
		battle.setId(1L);

		long databaseSizeBeforeCreate = getRepositoryCount();

		// An entity with an existing ID cannot be created, so this API call must fail
		restBattleMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(battle)))
			.andExpect(status().isBadRequest());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeCreate);
	}

	@Test
	@Transactional
	void getAllBattles() throws Exception {
		// Initialize the database
		insertedBattle = battleRepository.saveAndFlush(battle);

		// Get all the battleList
		restBattleMockMvc
			.perform(get(ENTITY_API_URL + "?sort=id,desc"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.[*].id").value(hasItem(battle.getId().intValue())))
			.andExpect(jsonPath("$.[*].model1Answer").value(hasItem(DEFAULT_MODEL_1_ANSWER)))
			.andExpect(jsonPath("$.[*].model2Answer").value(hasItem(DEFAULT_MODEL_2_ANSWER)))
			.andExpect(jsonPath("$.[*].voteTimestamp").value(hasItem(DEFAULT_VOTE_TIMESTAMP.toString())));
	}

	@Test
	@Transactional
	void getBattle() throws Exception {
		// Initialize the database
		insertedBattle = battleRepository.saveAndFlush(battle);

		// Get the battle
		restBattleMockMvc
			.perform(get(ENTITY_API_URL_ID, battle.getId()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.id").value(battle.getId().intValue()))
			.andExpect(jsonPath("$.model1Answer").value(DEFAULT_MODEL_1_ANSWER))
			.andExpect(jsonPath("$.model2Answer").value(DEFAULT_MODEL_2_ANSWER))
			.andExpect(jsonPath("$.voteTimestamp").value(DEFAULT_VOTE_TIMESTAMP.toString()));
	}

	@Test
	@Transactional
	void getNonExistingBattle() throws Exception {
		// Get the battle
		restBattleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	void putExistingBattle() throws Exception {
		// Initialize the database
		insertedBattle = battleRepository.saveAndFlush(battle);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the battle
		Battle updatedBattle = battleRepository.findById(battle.getId()).orElseThrow();
		// Disconnect from session so that the updates on updatedBattle are not directly saved in db
		em.detach(updatedBattle);
		updatedBattle.model1Answer(UPDATED_MODEL_1_ANSWER).model2Answer(UPDATED_MODEL_2_ANSWER).voteTimestamp(UPDATED_VOTE_TIMESTAMP);

		restBattleMockMvc
			.perform(
				put(ENTITY_API_URL_ID, updatedBattle.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(updatedBattle))
			)
			.andExpect(status().isOk());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertPersistedBattleToMatchAllProperties(updatedBattle);
	}

	@Test
	@Transactional
	void putNonExistingBattle() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		battle.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restBattleMockMvc
			.perform(put(ENTITY_API_URL_ID, battle.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(battle)))
			.andExpect(status().isBadRequest());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithIdMismatchBattle() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		battle.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restBattleMockMvc
			.perform(
				put(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(battle))
			)
			.andExpect(status().isBadRequest());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithMissingIdPathParamBattle() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		battle.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restBattleMockMvc
			.perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(battle)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void partialUpdateBattleWithPatch() throws Exception {
		// Initialize the database
		insertedBattle = battleRepository.saveAndFlush(battle);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the battle using partial update
		Battle partialUpdatedBattle = new Battle();
		partialUpdatedBattle.setId(battle.getId());

		restBattleMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedBattle.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedBattle))
			)
			.andExpect(status().isOk());

		// Validate the Battle in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertBattleUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBattle, battle), getPersistedBattle(battle));
	}

	@Test
	@Transactional
	void fullUpdateBattleWithPatch() throws Exception {
		// Initialize the database
		insertedBattle = battleRepository.saveAndFlush(battle);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the battle using partial update
		Battle partialUpdatedBattle = new Battle();
		partialUpdatedBattle.setId(battle.getId());

		partialUpdatedBattle
			.model1Answer(UPDATED_MODEL_1_ANSWER)
			.model2Answer(UPDATED_MODEL_2_ANSWER)
			.voteTimestamp(UPDATED_VOTE_TIMESTAMP);

		restBattleMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedBattle.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedBattle))
			)
			.andExpect(status().isOk());

		// Validate the Battle in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertBattleUpdatableFieldsEquals(partialUpdatedBattle, getPersistedBattle(partialUpdatedBattle));
	}

	@Test
	@Transactional
	void patchNonExistingBattle() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		battle.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restBattleMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, battle.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(battle))
			)
			.andExpect(status().isBadRequest());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithIdMismatchBattle() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		battle.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restBattleMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(battle))
			)
			.andExpect(status().isBadRequest());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithMissingIdPathParamBattle() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		battle.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restBattleMockMvc
			.perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(battle)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the Battle in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void deleteBattle() throws Exception {
		// Initialize the database
		insertedBattle = battleRepository.saveAndFlush(battle);

		long databaseSizeBeforeDelete = getRepositoryCount();

		// Delete the battle
		restBattleMockMvc
			.perform(delete(ENTITY_API_URL_ID, battle.getId()).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		// Validate the database contains one less item
		assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
	}

	protected long getRepositoryCount() {
		return battleRepository.count();
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

	protected Battle getPersistedBattle(Battle battle) {
		return battleRepository.findById(battle.getId()).orElseThrow();
	}

	protected void assertPersistedBattleToMatchAllProperties(Battle expectedBattle) {
		assertBattleAllPropertiesEquals(expectedBattle, getPersistedBattle(expectedBattle));
	}

	protected void assertPersistedBattleToMatchUpdatableProperties(Battle expectedBattle) {
		assertBattleAllUpdatablePropertiesEquals(expectedBattle, getPersistedBattle(expectedBattle));
	}
}
