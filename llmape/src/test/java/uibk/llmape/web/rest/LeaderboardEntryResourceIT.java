package uibk.llmape.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uibk.llmape.domain.LeaderboardEntryAsserts.*;
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
import uibk.llmape.domain.LeaderboardEntry;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.repository.LeaderboardEntryRepository;
import uibk.llmape.security.AuthoritiesConstants;

/**
 * Integration tests for the {@link LeaderboardEntryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class LeaderboardEntryResourceIT {

	private static final String DEFAULT_ENTRY_JSON = "AAAAAAAAAA";
	private static final String UPDATED_ENTRY_JSON = "BBBBBBBBBB";

	private static final Category DEFAULT_CATEGORY = Category.HINT_GENERATION;
	private static final Category UPDATED_CATEGORY = Category.EXERCISE_GENERATION;

	private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
	private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

	private static final String ENTITY_API_URL = "/api/leaderboard-entries";
	private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

	private static Random random = new Random();
	private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	@Autowired
	private ObjectMapper om;

	@Autowired
	private LeaderboardEntryRepository leaderboardEntryRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private MockMvc restLeaderboardEntryMockMvc;

	private LeaderboardEntry leaderboardEntry;

	private LeaderboardEntry insertedLeaderboardEntry;

	/**
	 * Create an entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static LeaderboardEntry createEntity() {
		return new LeaderboardEntry().entryJson(DEFAULT_ENTRY_JSON).category(DEFAULT_CATEGORY).timestamp(DEFAULT_TIMESTAMP);
	}

	/**
	 * Create an updated entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static LeaderboardEntry createUpdatedEntity() {
		return new LeaderboardEntry().entryJson(UPDATED_ENTRY_JSON).category(UPDATED_CATEGORY).timestamp(UPDATED_TIMESTAMP);
	}

	@BeforeEach
	public void initTest() {
		leaderboardEntry = createEntity();
	}

	@AfterEach
	public void cleanup() {
		if (insertedLeaderboardEntry != null) {
			leaderboardEntryRepository.delete(insertedLeaderboardEntry);
			insertedLeaderboardEntry = null;
		}
	}

	@Test
	@Transactional
	void createLeaderboardEntry() throws Exception {
		long databaseSizeBeforeCreate = getRepositoryCount();
		// Create the LeaderboardEntry
		var returnedLeaderboardEntry = om.readValue(
			restLeaderboardEntryMockMvc
				.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leaderboardEntry)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString(),
			LeaderboardEntry.class
		);

		// Validate the LeaderboardEntry in the database
		assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
		assertLeaderboardEntryUpdatableFieldsEquals(returnedLeaderboardEntry, getPersistedLeaderboardEntry(returnedLeaderboardEntry));

		insertedLeaderboardEntry = returnedLeaderboardEntry;
	}

	@Test
	@Transactional
	void createLeaderboardEntryWithExistingId() throws Exception {
		// Create the LeaderboardEntry with an existing ID
		leaderboardEntry.setId(1L);

		long databaseSizeBeforeCreate = getRepositoryCount();

		// An entity with an existing ID cannot be created, so this API call must fail
		restLeaderboardEntryMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leaderboardEntry)))
			.andExpect(status().isBadRequest());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeCreate);
	}

	@Test
	@Transactional
	void checkEntryJsonIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		leaderboardEntry.setEntryJson(null);

		// Create the LeaderboardEntry, which fails.

		restLeaderboardEntryMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leaderboardEntry)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkTimestampIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		leaderboardEntry.setTimestamp(null);

		// Create the LeaderboardEntry, which fails.

		restLeaderboardEntryMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leaderboardEntry)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void getAllLeaderboardEntries() throws Exception {
		// Initialize the database
		insertedLeaderboardEntry = leaderboardEntryRepository.saveAndFlush(leaderboardEntry);

		// Get all the leaderboardEntryList
		restLeaderboardEntryMockMvc
			.perform(get(ENTITY_API_URL + "?sort=id,desc"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.[*].id").value(hasItem(leaderboardEntry.getId().intValue())))
			.andExpect(jsonPath("$.[*].entryJson").value(hasItem(DEFAULT_ENTRY_JSON)))
			.andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
			.andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP.toString())));
	}

	@Test
	@Transactional
	void getLeaderboardEntry() throws Exception {
		// Initialize the database
		insertedLeaderboardEntry = leaderboardEntryRepository.saveAndFlush(leaderboardEntry);

		// Get the leaderboardEntry
		restLeaderboardEntryMockMvc
			.perform(get(ENTITY_API_URL_ID, leaderboardEntry.getId()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.id").value(leaderboardEntry.getId().intValue()))
			.andExpect(jsonPath("$.entryJson").value(DEFAULT_ENTRY_JSON))
			.andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
			.andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP.toString()));
	}

	@Test
	@Transactional
	void getNonExistingLeaderboardEntry() throws Exception {
		// Get the leaderboardEntry
		restLeaderboardEntryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	void putExistingLeaderboardEntry() throws Exception {
		// Initialize the database
		insertedLeaderboardEntry = leaderboardEntryRepository.saveAndFlush(leaderboardEntry);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the leaderboardEntry
		LeaderboardEntry updatedLeaderboardEntry = leaderboardEntryRepository.findById(leaderboardEntry.getId()).orElseThrow();
		// Disconnect from session so that the updates on updatedLeaderboardEntry are not directly saved in db
		em.detach(updatedLeaderboardEntry);
		updatedLeaderboardEntry.entryJson(UPDATED_ENTRY_JSON).category(UPDATED_CATEGORY).timestamp(UPDATED_TIMESTAMP);

		restLeaderboardEntryMockMvc
			.perform(
				put(ENTITY_API_URL_ID, updatedLeaderboardEntry.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(updatedLeaderboardEntry))
			)
			.andExpect(status().isOk());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertPersistedLeaderboardEntryToMatchAllProperties(updatedLeaderboardEntry);
	}

	@Test
	@Transactional
	void putNonExistingLeaderboardEntry() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		leaderboardEntry.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restLeaderboardEntryMockMvc
			.perform(
				put(ENTITY_API_URL_ID, leaderboardEntry.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(leaderboardEntry))
			)
			.andExpect(status().isBadRequest());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithIdMismatchLeaderboardEntry() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		leaderboardEntry.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restLeaderboardEntryMockMvc
			.perform(
				put(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(leaderboardEntry))
			)
			.andExpect(status().isBadRequest());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithMissingIdPathParamLeaderboardEntry() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		leaderboardEntry.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restLeaderboardEntryMockMvc
			.perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(leaderboardEntry)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void partialUpdateLeaderboardEntryWithPatch() throws Exception {
		// Initialize the database
		insertedLeaderboardEntry = leaderboardEntryRepository.saveAndFlush(leaderboardEntry);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the leaderboardEntry using partial update
		LeaderboardEntry partialUpdatedLeaderboardEntry = new LeaderboardEntry();
		partialUpdatedLeaderboardEntry.setId(leaderboardEntry.getId());

		partialUpdatedLeaderboardEntry.entryJson(UPDATED_ENTRY_JSON).category(UPDATED_CATEGORY).timestamp(UPDATED_TIMESTAMP);

		restLeaderboardEntryMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedLeaderboardEntry.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedLeaderboardEntry))
			)
			.andExpect(status().isOk());

		// Validate the LeaderboardEntry in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertLeaderboardEntryUpdatableFieldsEquals(
			createUpdateProxyForBean(partialUpdatedLeaderboardEntry, leaderboardEntry),
			getPersistedLeaderboardEntry(leaderboardEntry)
		);
	}

	@Test
	@Transactional
	void fullUpdateLeaderboardEntryWithPatch() throws Exception {
		// Initialize the database
		insertedLeaderboardEntry = leaderboardEntryRepository.saveAndFlush(leaderboardEntry);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the leaderboardEntry using partial update
		LeaderboardEntry partialUpdatedLeaderboardEntry = new LeaderboardEntry();
		partialUpdatedLeaderboardEntry.setId(leaderboardEntry.getId());

		partialUpdatedLeaderboardEntry.entryJson(UPDATED_ENTRY_JSON).category(UPDATED_CATEGORY).timestamp(UPDATED_TIMESTAMP);

		restLeaderboardEntryMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedLeaderboardEntry.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedLeaderboardEntry))
			)
			.andExpect(status().isOk());

		// Validate the LeaderboardEntry in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertLeaderboardEntryUpdatableFieldsEquals(
			partialUpdatedLeaderboardEntry,
			getPersistedLeaderboardEntry(partialUpdatedLeaderboardEntry)
		);
	}

	@Test
	@Transactional
	void patchNonExistingLeaderboardEntry() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		leaderboardEntry.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restLeaderboardEntryMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, leaderboardEntry.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(leaderboardEntry))
			)
			.andExpect(status().isBadRequest());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithIdMismatchLeaderboardEntry() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		leaderboardEntry.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restLeaderboardEntryMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(leaderboardEntry))
			)
			.andExpect(status().isBadRequest());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithMissingIdPathParamLeaderboardEntry() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		leaderboardEntry.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restLeaderboardEntryMockMvc
			.perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(leaderboardEntry)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the LeaderboardEntry in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void deleteLeaderboardEntry() throws Exception {
		// Initialize the database
		insertedLeaderboardEntry = leaderboardEntryRepository.saveAndFlush(leaderboardEntry);

		long databaseSizeBeforeDelete = getRepositoryCount();

		// Delete the leaderboardEntry
		restLeaderboardEntryMockMvc
			.perform(delete(ENTITY_API_URL_ID, leaderboardEntry.getId()).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		// Validate the database contains one less item
		assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
	}

	protected long getRepositoryCount() {
		return leaderboardEntryRepository.count();
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

	protected LeaderboardEntry getPersistedLeaderboardEntry(LeaderboardEntry leaderboardEntry) {
		return leaderboardEntryRepository.findById(leaderboardEntry.getId()).orElseThrow();
	}

	protected void assertPersistedLeaderboardEntryToMatchAllProperties(LeaderboardEntry expectedLeaderboardEntry) {
		assertLeaderboardEntryAllPropertiesEquals(expectedLeaderboardEntry, getPersistedLeaderboardEntry(expectedLeaderboardEntry));
	}

	protected void assertPersistedLeaderboardEntryToMatchUpdatableProperties(LeaderboardEntry expectedLeaderboardEntry) {
		assertLeaderboardEntryAllUpdatablePropertiesEquals(
			expectedLeaderboardEntry,
			getPersistedLeaderboardEntry(expectedLeaderboardEntry)
		);
	}
}
