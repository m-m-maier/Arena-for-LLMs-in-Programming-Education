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
import uibk.llmape.domain.Battle;
import uibk.llmape.repository.BattleRepository;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uibk.llmape.domain.Battle}.
 */
@RestController
@RequestMapping("/api/battles")
@Transactional
public class BattleResource {

	private static final Logger LOG = LoggerFactory.getLogger(BattleResource.class);

	private static final String ENTITY_NAME = "battle";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final BattleRepository battleRepository;

	public BattleResource(BattleRepository battleRepository) {
		this.battleRepository = battleRepository;
	}

	/**
	 * {@code POST  /battles} : Create a new battle.
	 *
	 * @param battle the battle to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new battle, or with status {@code 400 (Bad Request)} if the battle has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("")
	public ResponseEntity<Battle> createBattle(@Valid @RequestBody Battle battle) throws URISyntaxException {
		LOG.debug("REST request to save Battle : {}", battle);
		if (battle.getId() != null) {
			throw new BadRequestAlertException("A new battle cannot already have an ID", ENTITY_NAME, "idexists");
		}
		battle = battleRepository.save(battle);
		return ResponseEntity.created(new URI("/api/battles/" + battle.getId()))
			.headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, battle.getId().toString()))
			.body(battle);
	}

	/**
	 * {@code PUT  /battles/:id} : Updates an existing battle.
	 *
	 * @param id the id of the battle to save.
	 * @param battle the battle to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated battle,
	 * or with status {@code 400 (Bad Request)} if the battle is not valid,
	 * or with status {@code 500 (Internal Server Error)} if the battle couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Battle> updateBattle(
		@PathVariable(value = "id", required = false) final Long id,
		@Valid @RequestBody Battle battle
	) throws URISyntaxException {
		LOG.debug("REST request to update Battle : {}, {}", id, battle);
		if (battle.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, battle.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!battleRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		battle = battleRepository.save(battle);
		return ResponseEntity.ok()
			.headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, battle.getId().toString()))
			.body(battle);
	}

	/**
	 * {@code PATCH  /battles/:id} : Partial updates given fields of an existing battle, field will ignore if it is null
	 *
	 * @param id the id of the battle to save.
	 * @param battle the battle to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated battle,
	 * or with status {@code 400 (Bad Request)} if the battle is not valid,
	 * or with status {@code 404 (Not Found)} if the battle is not found,
	 * or with status {@code 500 (Internal Server Error)} if the battle couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
	public ResponseEntity<Battle> partialUpdateBattle(
		@PathVariable(value = "id", required = false) final Long id,
		@NotNull @RequestBody Battle battle
	) throws URISyntaxException {
		LOG.debug("REST request to partial update Battle partially : {}, {}", id, battle);
		if (battle.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, battle.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (!battleRepository.existsById(id)) {
			throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
		}

		Optional<Battle> result = battleRepository
			.findById(battle.getId())
			.map(existingBattle -> {
				if (battle.getModel1Answer() != null) {
					existingBattle.setModel1Answer(battle.getModel1Answer());
				}
				if (battle.getModel2Answer() != null) {
					existingBattle.setModel2Answer(battle.getModel2Answer());
				}
				if (battle.getVoteTimestamp() != null) {
					existingBattle.setVoteTimestamp(battle.getVoteTimestamp());
				}

				return existingBattle;
			})
			.map(battleRepository::save);

		return ResponseUtil.wrapOrNotFound(
			result,
			HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, battle.getId().toString())
		);
	}

	/**
	 * {@code GET  /battles} : get all the battles.
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of battles in body.
	 */
	@GetMapping("")
	public List<Battle> getAllBattles() {
		LOG.debug("REST request to get all Battles");
		return battleRepository.findAll();
	}

	/**
	 * {@code GET  /battles/:id} : get the "id" battle.
	 *
	 * @param id the id of the battle to retrieve.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the battle, or with status {@code 404 (Not Found)}.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Battle> getBattle(@PathVariable("id") Long id) {
		LOG.debug("REST request to get Battle : {}", id);
		Optional<Battle> battle = battleRepository.findById(id);
		return ResponseUtil.wrapOrNotFound(battle);
	}

	/**
	 * {@code DELETE  /battles/:id} : delete the "id" battle.
	 *
	 * @param id the id of the battle to delete.
	 * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBattle(@PathVariable("id") Long id) {
		LOG.debug("REST request to delete Battle : {}", id);
		battleRepository.deleteById(id);
		return ResponseEntity.noContent()
			.headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
			.build();
	}
}
