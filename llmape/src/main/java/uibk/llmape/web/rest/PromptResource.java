package uibk.llmape.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import uibk.llmape.domain.Prompt;
import uibk.llmape.repository.PromptRepository;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uibk.llmape.domain.Prompt}.
 */
@RestController
@RequestMapping("/api/prompts")
@Transactional
public class PromptResource {

	private static final Logger LOG = LoggerFactory.getLogger(PromptResource.class);

	private static final String ENTITY_NAME = "prompt";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final PromptRepository promptRepository;

	public PromptResource(PromptRepository promptRepository) {
		this.promptRepository = promptRepository;
	}

	/**
	 * {@code POST  /prompts} : Create a new prompt.
	 *
	 * @param prompt the prompt to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prompt, or with status {@code 400 (Bad Request)} if the prompt has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("")
	public ResponseEntity<Prompt> createPrompt(@Valid @RequestBody Prompt prompt) throws URISyntaxException {
		LOG.debug("REST request to save Prompt : {}", prompt);
		if (prompt.getId() != null) {
			throw new BadRequestAlertException("A new prompt cannot already have an ID", ENTITY_NAME, "idexists");
		}
		prompt = promptRepository.save(prompt);
		return ResponseEntity.created(new URI("/api/prompts/" + prompt.getId()))
			.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prompt.getId().toString()))
			.body(prompt);
	}

	/**
	 * {@code PUT  /prompts/:id} : Updates an existing prompt.
	 *
	 * @param id the id of the prompt to save.
	 * @param prompt the prompt to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prompt,
	 * or with status {@code 400 (Bad Request)} if the prompt is not valid,
	 * or with status {@code 500 (Internal Server Error)} if the prompt couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Prompt> updatePrompt(
		@PathVariable(value = "id", required = false) final Long id,
		@Valid @RequestBody Prompt prompt
	) throws URISyntaxException {
		LOG.debug("REST request to update Prompt : {}, {}", id, prompt);
		if (prompt.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, prompt.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!promptRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		prompt = promptRepository.save(prompt);
		return ResponseEntity.ok()
			.headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prompt.getId().toString()))
			.body(prompt);
	}

	/**
	 * {@code PATCH  /prompts/:id} : Partial updates given fields of an existing prompt, field will ignore if it is null
	 *
	 * @param id the id of the prompt to save.
	 * @param prompt the prompt to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prompt,
	 * or with status {@code 400 (Bad Request)} if the prompt is not valid,
	 * or with status {@code 404 (Not Found)} if the prompt is not found,
	 * or with status {@code 500 (Internal Server Error)} if the prompt couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
	public ResponseEntity<Prompt> partialUpdatePrompt(
		@PathVariable(value = "id", required = false) final Long id,
		@NotNull @RequestBody Prompt prompt
	) throws URISyntaxException {
		LOG.debug("REST request to partial update Prompt partially : {}, {}", id, prompt);
		if (prompt.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, prompt.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!promptRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		Optional<Prompt> result = promptRepository
			.findById(prompt.getId())
			.map(existingPrompt -> {
				if (prompt.getPromptText() != null) {
					existingPrompt.setPromptText(prompt.getPromptText());
				}
				if (prompt.getCategory() != null) {
					existingPrompt.setCategory(prompt.getCategory());
				}
				if (prompt.getIsRejected() != null) {
					existingPrompt.setIsRejected(prompt.getIsRejected());
				}
				if (prompt.getIsFromPublicPage() != null) {
					existingPrompt.setIsFromPublicPage(prompt.getIsFromPublicPage());
				}
				if (prompt.getTimestamp() != null) {
					existingPrompt.setTimestamp(prompt.getTimestamp());
				}
				if (prompt.getSessionId() != null) {
					existingPrompt.setSessionId(prompt.getSessionId());
				}
				if (prompt.getGenerationModelId() != null) {
					existingPrompt.setGenerationModelId(prompt.getGenerationModelId());
				}

				return existingPrompt;
			})
			.map(promptRepository::save);

		return ResponseUtil.wrapOrNotFound(
			result,
			HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prompt.getId().toString())
		);
	}

	/**
	 * {@code GET  /prompts} : get all the prompts.
	 *
	 * @param filter the filter of the request.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of prompts in body.
	 */
	@GetMapping("")
	public List<Prompt> getAllPrompts(@RequestParam(name = "filter", required = false) String filter) {
		if ("battle-is-null".equals(filter)) {
			LOG.debug("REST request to get all Prompts where battle is null");
			return StreamSupport.stream(promptRepository.findAll().spliterator(), false)
				.filter(prompt -> prompt.getBattle() == null)
				.toList();
		}
		LOG.debug("REST request to get all Prompts");
		return promptRepository.findAll();
	}

	/**
	 * {@code GET  /prompts/:id} : get the "id" prompt.
	 *
	 * @param id the id of the prompt to retrieve.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the prompt, or with status {@code 404 (Not Found)}.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Prompt> getPrompt(@PathVariable("id") Long id) {
		LOG.debug("REST request to get Prompt : {}", id);
		Optional<Prompt> prompt = promptRepository.findById(id);
		return ResponseUtil.wrapOrNotFound(prompt);
	}

	/**
	 * {@code DELETE  /prompts/:id} : delete the "id" prompt.
	 *
	 * @param id the id of the prompt to delete.
	 * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePrompt(@PathVariable("id") Long id) {
		LOG.debug("REST request to delete Prompt : {}", id);
		promptRepository.deleteById(id);
		return ResponseEntity.noContent()
			.headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
			.build();
	}
}
