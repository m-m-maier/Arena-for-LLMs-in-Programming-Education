package uibk.llmape.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import uibk.llmape.domain.Model;
import uibk.llmape.repository.ModelRepository;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uibk.llmape.domain.Model}.
 */
@RestController
@RequestMapping("/api/models")
@Transactional
public class ModelResource {

	private static final Logger LOG = LoggerFactory.getLogger(ModelResource.class);

	private static final String ENTITY_NAME = "model";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final ModelRepository modelRepository;

	public ModelResource(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * {@code POST  /models} : Create a new model.
	 *
	 * @param model the model to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new model, or with status {@code 400 (Bad Request)} if the model has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("")
	public ResponseEntity<Model> createModel(@Valid @RequestBody Model model) throws URISyntaxException {
		LOG.debug("REST request to save Model : {}", model);
		if (model.getId() != null) {
			throw new BadRequestAlertException("A new model cannot already have an ID", ENTITY_NAME, "idexists");
		}
		model = modelRepository.save(model);
		return ResponseEntity.created(new URI("/api/models/" + model.getId()))
			.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, model.getId().toString()))
			.body(model);
	}

	/**
	 * {@code PUT  /models/:id} : Updates an existing model.
	 *
	 * @param id the id of the model to save.
	 * @param model the model to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated model,
	 * or with status {@code 400 (Bad Request)} if the model is not valid,
	 * or with status {@code 500 (Internal Server Error)} if the model couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Model> updateModel(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Model model)
		throws URISyntaxException {
		LOG.debug("REST request to update Model : {}, {}", id, model);
		if (model.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, model.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!modelRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		model = modelRepository.save(model);
		return ResponseEntity.ok()
			.headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, model.getId().toString()))
			.body(model);
	}

	/**
	 * {@code PATCH  /models/:id} : Partial updates given fields of an existing model, field will ignore if it is null
	 *
	 * @param id the id of the model to save.
	 * @param model the model to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated model,
	 * or with status {@code 400 (Bad Request)} if the model is not valid,
	 * or with status {@code 404 (Not Found)} if the model is not found,
	 * or with status {@code 500 (Internal Server Error)} if the model couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
	public ResponseEntity<Model> partialUpdateModel(
		@PathVariable(value = "id", required = false) final Long id,
		@NotNull @RequestBody Model model
	) throws URISyntaxException {
		LOG.debug("REST request to partial update Model partially : {}, {}", id, model);
		if (model.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, model.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!modelRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		Optional<Model> result = modelRepository
			.findById(model.getId())
			.map(existingModel -> {
				if (model.getModelName() != null) {
					existingModel.setModelName(model.getModelName());
				}
				if (model.getOrganization() != null) {
					existingModel.setOrganization(model.getOrganization());
				}
				if (model.getProvider() != null) {
					existingModel.setProvider(model.getProvider());
				}
				if (model.getApiKey() != null) {
					existingModel.setApiKey(model.getApiKey());
				}
				if (model.getBaseUrl() != null) {
					existingModel.setBaseUrl(model.getBaseUrl());
				}
				if (model.getLicense() != null) {
					existingModel.setLicense(model.getLicense());
				}
				if (model.getActive() != null) {
					existingModel.setActive(model.getActive());
				}

				return existingModel;
			})
			.map(modelRepository::save);

		return ResponseUtil.wrapOrNotFound(
			result,
			HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, model.getId().toString())
		);
	}

	/**
	 * {@code GET  /models} : get all the models.
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of models in body.
	 */
	@GetMapping("")
	public List<Model> getAllModels() {
		LOG.debug("REST request to get all Models");
		return modelRepository.findAll();
	}

	/**
	 * {@code GET  /models/:id} : get the "id" model.
	 *
	 * @param id the id of the model to retrieve.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the model, or with status {@code 404 (Not Found)}.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Model> getModel(@PathVariable("id") Long id) {
		LOG.debug("REST request to get Model : {}", id);
		Optional<Model> model = modelRepository.findById(id);
		return ResponseUtil.wrapOrNotFound(model);
	}

	/**
	 * {@code DELETE  /models/:id} : delete the "id" model.
	 *
	 * @param id the id of the model to delete.
	 * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteModel(@PathVariable("id") Long id) {
		LOG.debug("REST request to delete Model : {}", id);
		modelRepository.deleteById(id);
		return ResponseEntity.noContent()
			.headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
			.build();
	}
}
