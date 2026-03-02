package uibk.llmape.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uibk.llmape.domain.ModelAsserts.*;
import static uibk.llmape.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import uibk.llmape.domain.Model;
import uibk.llmape.repository.ModelRepository;
import uibk.llmape.security.AuthoritiesConstants;

/**
 * Integration tests for the {@link ModelResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ModelResourceIT {

	private static final String DEFAULT_MODEL_NAME = "AAAAAAAAAA";
	private static final String UPDATED_MODEL_NAME = "BBBBBBBBBB";

	private static final String DEFAULT_ORGANIZATION = "AAAAAAAAAA";
	private static final String UPDATED_ORGANIZATION = "BBBBBBBBBB";

	private static final String DEFAULT_PROVIDER = "AAAAAAAAAA";
	private static final String UPDATED_PROVIDER = "BBBBBBBBBB";

	private static final String DEFAULT_API_KEY = "AAAAAAAAAA";
	private static final String UPDATED_API_KEY = "BBBBBBBBBB";

	private static final String DEFAULT_BASE_URL = "AAAAAAAAAA";
	private static final String UPDATED_BASE_URL = "BBBBBBBBBB";

	private static final String DEFAULT_LICENSE = "AAAAAAAAAA";
	private static final String UPDATED_LICENSE = "BBBBBBBBBB";

	private static final Boolean DEFAULT_ACTIVE = false;
	private static final Boolean UPDATED_ACTIVE = true;

	private static final String ENTITY_API_URL = "/api/models";
	private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

	private static Random random = new Random();
	private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	@Autowired
	private ObjectMapper om;

	@Autowired
	private ModelRepository modelRepository;

	@Autowired
	private EntityManager em;

	@Autowired
	private MockMvc restModelMockMvc;

	private Model model;

	private Model insertedModel;

	/**
	 * Create an entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Model createEntity() {
		return new Model()
			.modelName(DEFAULT_MODEL_NAME)
			.organization(DEFAULT_ORGANIZATION)
			.provider(DEFAULT_PROVIDER)
			.apiKey(DEFAULT_API_KEY)
			.baseUrl(DEFAULT_BASE_URL)
			.license(DEFAULT_LICENSE)
			.active(DEFAULT_ACTIVE);
	}

	/**
	 * Create an updated entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it,
	 * if they test an entity which requires the current entity.
	 */
	public static Model createUpdatedEntity() {
		return new Model()
			.modelName(UPDATED_MODEL_NAME)
			.organization(UPDATED_ORGANIZATION)
			.provider(UPDATED_PROVIDER)
			.apiKey(UPDATED_API_KEY)
			.baseUrl(UPDATED_BASE_URL)
			.license(UPDATED_LICENSE)
			.active(UPDATED_ACTIVE);
	}

	@BeforeEach
	public void initTest() {
		model = createEntity();
	}

	@AfterEach
	public void cleanup() {
		if (insertedModel != null) {
			modelRepository.delete(insertedModel);
			insertedModel = null;
		}
	}

	@Test
	@Transactional
	void createModel() throws Exception {
		long databaseSizeBeforeCreate = getRepositoryCount();
		// Create the Model
		var returnedModel = om.readValue(
			restModelMockMvc
				.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString(),
			Model.class
		);

		// Validate the Model in the database
		assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
		assertModelUpdatableFieldsEquals(returnedModel, getPersistedModel(returnedModel));

		insertedModel = returnedModel;
	}

	@Test
	@Transactional
	void createModelWithExistingId() throws Exception {
		// Create the Model with an existing ID
		model.setId(1L);

		long databaseSizeBeforeCreate = getRepositoryCount();

		// An entity with an existing ID cannot be created, so this API call must fail
		restModelMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeCreate);
	}

	@Test
	@Transactional
	void checkModelNameIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		model.setModelName(null);

		// Create the Model, which fails.

		restModelMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkOrganizationIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		model.setOrganization(null);

		// Create the Model, which fails.

		restModelMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkProviderIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		model.setProvider(null);

		// Create the Model, which fails.

		restModelMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkLicenseIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		model.setLicense(null);

		// Create the Model, which fails.

		restModelMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void checkActiveIsRequired() throws Exception {
		long databaseSizeBeforeTest = getRepositoryCount();
		// set the field null
		model.setActive(null);

		// Create the Model, which fails.

		restModelMockMvc
			.perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		assertSameRepositoryCount(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	void getAllModels() throws Exception {
		// Initialize the database
		insertedModel = modelRepository.saveAndFlush(model);

		// Get all the modelList
		restModelMockMvc
			.perform(get(ENTITY_API_URL + "?sort=id,desc"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.[*].id").value(hasItem(model.getId().intValue())))
			.andExpect(jsonPath("$.[*].modelName").value(hasItem(DEFAULT_MODEL_NAME)))
			.andExpect(jsonPath("$.[*].organization").value(hasItem(DEFAULT_ORGANIZATION)))
			.andExpect(jsonPath("$.[*].provider").value(hasItem(DEFAULT_PROVIDER)))
			.andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
			.andExpect(jsonPath("$.[*].baseUrl").value(hasItem(DEFAULT_BASE_URL)))
			.andExpect(jsonPath("$.[*].license").value(hasItem(DEFAULT_LICENSE)))
			.andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
	}

	@Test
	@Transactional
	void getModel() throws Exception {
		// Initialize the database
		insertedModel = modelRepository.saveAndFlush(model);

		// Get the model
		restModelMockMvc
			.perform(get(ENTITY_API_URL_ID, model.getId()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.id").value(model.getId().intValue()))
			.andExpect(jsonPath("$.modelName").value(DEFAULT_MODEL_NAME))
			.andExpect(jsonPath("$.organization").value(DEFAULT_ORGANIZATION))
			.andExpect(jsonPath("$.provider").value(DEFAULT_PROVIDER))
			.andExpect(jsonPath("$.apiKey").value(DEFAULT_API_KEY))
			.andExpect(jsonPath("$.baseUrl").value(DEFAULT_BASE_URL))
			.andExpect(jsonPath("$.license").value(DEFAULT_LICENSE))
			.andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
	}

	@Test
	@Transactional
	void getNonExistingModel() throws Exception {
		// Get the model
		restModelMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	void putExistingModel() throws Exception {
		// Initialize the database
		insertedModel = modelRepository.saveAndFlush(model);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the model
		Model updatedModel = modelRepository.findById(model.getId()).orElseThrow();
		// Disconnect from session so that the updates on updatedModel are not directly saved in db
		em.detach(updatedModel);
		updatedModel
			.modelName(UPDATED_MODEL_NAME)
			.organization(UPDATED_ORGANIZATION)
			.provider(UPDATED_PROVIDER)
			.apiKey(UPDATED_API_KEY)
			.baseUrl(UPDATED_BASE_URL)
			.license(UPDATED_LICENSE)
			.active(UPDATED_ACTIVE);

		restModelMockMvc
			.perform(
				put(ENTITY_API_URL_ID, updatedModel.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(updatedModel))
			)
			.andExpect(status().isOk());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertPersistedModelToMatchAllProperties(updatedModel);
	}

	@Test
	@Transactional
	void putNonExistingModel() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		model.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restModelMockMvc
			.perform(put(ENTITY_API_URL_ID, model.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isBadRequest());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithIdMismatchModel() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		model.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restModelMockMvc
			.perform(
				put(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType(MediaType.APPLICATION_JSON)
					.content(om.writeValueAsBytes(model))
			)
			.andExpect(status().isBadRequest());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void putWithMissingIdPathParamModel() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		model.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restModelMockMvc
			.perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(model)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void partialUpdateModelWithPatch() throws Exception {
		// Initialize the database
		insertedModel = modelRepository.saveAndFlush(model);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the model using partial update
		Model partialUpdatedModel = new Model();
		partialUpdatedModel.setId(model.getId());

		partialUpdatedModel
			.modelName(UPDATED_MODEL_NAME)
			.organization(UPDATED_ORGANIZATION)
			.provider(UPDATED_PROVIDER)
			.license(UPDATED_LICENSE)
			.active(UPDATED_ACTIVE);

		restModelMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedModel.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedModel))
			)
			.andExpect(status().isOk());

		// Validate the Model in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertModelUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedModel, model), getPersistedModel(model));
	}

	@Test
	@Transactional
	void fullUpdateModelWithPatch() throws Exception {
		// Initialize the database
		insertedModel = modelRepository.saveAndFlush(model);

		long databaseSizeBeforeUpdate = getRepositoryCount();

		// Update the model using partial update
		Model partialUpdatedModel = new Model();
		partialUpdatedModel.setId(model.getId());

		partialUpdatedModel
			.modelName(UPDATED_MODEL_NAME)
			.organization(UPDATED_ORGANIZATION)
			.provider(UPDATED_PROVIDER)
			.apiKey(UPDATED_API_KEY)
			.baseUrl(UPDATED_BASE_URL)
			.license(UPDATED_LICENSE)
			.active(UPDATED_ACTIVE);

		restModelMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, partialUpdatedModel.getId())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(partialUpdatedModel))
			)
			.andExpect(status().isOk());

		// Validate the Model in the database

		assertSameRepositoryCount(databaseSizeBeforeUpdate);
		assertModelUpdatableFieldsEquals(partialUpdatedModel, getPersistedModel(partialUpdatedModel));
	}

	@Test
	@Transactional
	void patchNonExistingModel() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		model.setId(longCount.incrementAndGet());

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restModelMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, model.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(model))
			)
			.andExpect(status().isBadRequest());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithIdMismatchModel() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		model.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restModelMockMvc
			.perform(
				patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
					.contentType("application/merge-patch+json")
					.content(om.writeValueAsBytes(model))
			)
			.andExpect(status().isBadRequest());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void patchWithMissingIdPathParamModel() throws Exception {
		long databaseSizeBeforeUpdate = getRepositoryCount();
		model.setId(longCount.incrementAndGet());

		// If url ID doesn't match entity ID, it will throw BadRequestAlertException
		restModelMockMvc
			.perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(model)))
			.andExpect(status().isMethodNotAllowed());

		// Validate the Model in the database
		assertSameRepositoryCount(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	void deleteModel() throws Exception {
		// Initialize the database
		insertedModel = modelRepository.saveAndFlush(model);

		long databaseSizeBeforeDelete = getRepositoryCount();

		// Delete the model
		restModelMockMvc
			.perform(delete(ENTITY_API_URL_ID, model.getId()).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		// Validate the database contains one less item
		assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
	}

	protected long getRepositoryCount() {
		return modelRepository.count();
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

	protected Model getPersistedModel(Model model) {
		return modelRepository.findById(model.getId()).orElseThrow();
	}

	protected void assertPersistedModelToMatchAllProperties(Model expectedModel) {
		assertModelAllPropertiesEquals(expectedModel, getPersistedModel(expectedModel));
	}

	protected void assertPersistedModelToMatchUpdatableProperties(Model expectedModel) {
		assertModelAllUpdatablePropertiesEquals(expectedModel, getPersistedModel(expectedModel));
	}
}
