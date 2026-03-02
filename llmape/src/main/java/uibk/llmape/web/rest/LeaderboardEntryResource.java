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
import uibk.llmape.domain.LeaderboardEntry;
import uibk.llmape.repository.LeaderboardEntryRepository;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uibk.llmape.domain.LeaderboardEntry}.
 */
@RestController
@RequestMapping("/api/leaderboard-entries")
@Transactional
public class LeaderboardEntryResource {

	private static final Logger LOG = LoggerFactory.getLogger(LeaderboardEntryResource.class);

	private static final String ENTITY_NAME = "leaderboardEntry";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final LeaderboardEntryRepository leaderboardEntryRepository;

	public LeaderboardEntryResource(LeaderboardEntryRepository leaderboardEntryRepository) {
		this.leaderboardEntryRepository = leaderboardEntryRepository;
	}

	/**
	 * {@code POST  /leaderboard-entries} : Create a new leaderboardEntry.
	 *
	 * @param leaderboardEntry the leaderboardEntry to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new leaderboardEntry, or with status {@code 400 (Bad Request)} if the leaderboardEntry has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("")
	public ResponseEntity<LeaderboardEntry> createLeaderboardEntry(@Valid @RequestBody LeaderboardEntry leaderboardEntry)
		throws URISyntaxException {
		LOG.debug("REST request to save LeaderboardEntry : {}", leaderboardEntry);
		if (leaderboardEntry.getId() != null) {
			throw new BadRequestAlertException("A new leaderboardEntry cannot already have an ID", ENTITY_NAME, "idexists");
		}
		leaderboardEntry = leaderboardEntryRepository.save(leaderboardEntry);
		return ResponseEntity.created(new URI("/api/leaderboard-entries/" + leaderboardEntry.getId()))
			.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, leaderboardEntry.getId().toString()))
			.body(leaderboardEntry);
	}

	/**
	 * {@code PUT  /leaderboard-entries/:id} : Updates an existing leaderboardEntry.
	 *
	 * @param id the id of the leaderboardEntry to save.
	 * @param leaderboardEntry the leaderboardEntry to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated leaderboardEntry,
	 * or with status {@code 400 (Bad Request)} if the leaderboardEntry is not valid,
	 * or with status {@code 500 (Internal Server Error)} if the leaderboardEntry couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<LeaderboardEntry> updateLeaderboardEntry(
		@PathVariable(value = "id", required = false) final Long id,
		@Valid @RequestBody LeaderboardEntry leaderboardEntry
	) throws URISyntaxException {
		LOG.debug("REST request to update LeaderboardEntry : {}, {}", id, leaderboardEntry);
		if (leaderboardEntry.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, leaderboardEntry.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!leaderboardEntryRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		leaderboardEntry = leaderboardEntryRepository.save(leaderboardEntry);
		return ResponseEntity.ok()
			.headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, leaderboardEntry.getId().toString()))
			.body(leaderboardEntry);
	}

	/**
	 * {@code PATCH  /leaderboard-entries/:id} : Partial updates given fields of an existing leaderboardEntry, field will ignore if it is null
	 *
	 * @param id the id of the leaderboardEntry to save.
	 * @param leaderboardEntry the leaderboardEntry to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated leaderboardEntry,
	 * or with status {@code 400 (Bad Request)} if the leaderboardEntry is not valid,
	 * or with status {@code 404 (Not Found)} if the leaderboardEntry is not found,
	 * or with status {@code 500 (Internal Server Error)} if the leaderboardEntry couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
	public ResponseEntity<LeaderboardEntry> partialUpdateLeaderboardEntry(
		@PathVariable(value = "id", required = false) final Long id,
		@NotNull @RequestBody LeaderboardEntry leaderboardEntry
	) throws URISyntaxException {
		LOG.debug("REST request to partial update LeaderboardEntry partially : {}, {}", id, leaderboardEntry);
		if (leaderboardEntry.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, leaderboardEntry.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!leaderboardEntryRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		Optional<LeaderboardEntry> result = leaderboardEntryRepository
			.findById(leaderboardEntry.getId())
			.map(existingLeaderboardEntry -> {
				if (leaderboardEntry.getEntryJson() != null) {
					existingLeaderboardEntry.setEntryJson(leaderboardEntry.getEntryJson());
				}
				if (leaderboardEntry.getCategory() != null) {
					existingLeaderboardEntry.setCategory(leaderboardEntry.getCategory());
				}
				if (leaderboardEntry.getTimestamp() != null) {
					existingLeaderboardEntry.setTimestamp(leaderboardEntry.getTimestamp());
				}

				return existingLeaderboardEntry;
			})
			.map(leaderboardEntryRepository::save);

		return ResponseUtil.wrapOrNotFound(
			result,
			HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, leaderboardEntry.getId().toString())
		);
	}

	/**
	 * {@code GET  /leaderboard-entries} : get all the leaderboardEntries.
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of leaderboardEntries in body.
	 */
	@GetMapping("")
	public List<LeaderboardEntry> getAllLeaderboardEntries() {
		LOG.debug("REST request to get all LeaderboardEntries");
		return leaderboardEntryRepository.findAll();
	}

	/**
	 * {@code GET  /leaderboard-entries/:id} : get the "id" leaderboardEntry.
	 *
	 * @param id the id of the leaderboardEntry to retrieve.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the leaderboardEntry, or with status {@code 404 (Not Found)}.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<LeaderboardEntry> getLeaderboardEntry(@PathVariable("id") Long id) {
		LOG.debug("REST request to get LeaderboardEntry : {}", id);
		Optional<LeaderboardEntry> leaderboardEntry = leaderboardEntryRepository.findById(id);
		return ResponseUtil.wrapOrNotFound(leaderboardEntry);
	}

	/**
	 * {@code DELETE  /leaderboard-entries/:id} : delete the "id" leaderboardEntry.
	 *
	 * @param id the id of the leaderboardEntry to delete.
	 * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteLeaderboardEntry(@PathVariable("id") Long id) {
		LOG.debug("REST request to delete LeaderboardEntry : {}", id);
		leaderboardEntryRepository.deleteById(id);
		return ResponseEntity.noContent()
			.headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
			.build();
	}
}
