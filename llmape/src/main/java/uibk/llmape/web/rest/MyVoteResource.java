package uibk.llmape.web.rest;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.HeaderUtil;
import uibk.llmape.repository.BattleRepository;
import uibk.llmape.service.battle.BattleService;
import uibk.llmape.service.battle.ModelIdentityDTO;
import uibk.llmape.service.battle.VoteDTO;
import uibk.llmape.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing votes.
 */
@RestController
@RequestMapping("/api/myvote")
@Transactional
public class MyVoteResource {

	private static final Logger LOG = LoggerFactory.getLogger(MyVoteResource.class);

	private static final String ENTITY_NAME = "battle";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final BattleService battleService;

    private final BattleRepository battleRepository;

	public MyVoteResource(BattleService battleService, BattleRepository battleRepository) {
		this.battleService = battleService;
        this.battleRepository = battleRepository;
	}

	/**
	 * {@code POST} : Vote for a preferred response or a tie of a battle with given id.
	 *
	 * @param voteDTO the voteDTO that contains information about the vote decision.
	 * @return the {@link ResponseEntity} with status {@code 201 (Updated)} and with body the two models' identities that participated in the battle, or with status {@code 400 (Bad Request)} if the battle was not found.
	 */
	@PostMapping("submitVote")
	public ResponseEntity<ModelIdentityDTO> submitVote(@Valid @RequestBody VoteDTO voteDTO) {
		LOG.debug("REST request to submit vote: {}", voteDTO);

        var optionalBattle = battleRepository.findById(voteDTO.getBattleId());
        var battle = optionalBattle.orElseThrow(() -> new BadRequestAlertException("Battle not found", "battle", "idnotfound"));

		var savedBattle = battleService.submitVote(battle, voteDTO.getVoteOption());

		var modelIdentityDto = new ModelIdentityDTO(savedBattle.getModel1().getModelName(), savedBattle.getModel2().getModelName());

		return ResponseEntity.ok()
			.headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, savedBattle.getId().toString()))
			.body(modelIdentityDto);
	}
}
