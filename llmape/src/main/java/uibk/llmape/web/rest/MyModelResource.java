package uibk.llmape.web.rest;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uibk.llmape.repository.ModelRepository;
import uibk.llmape.service.dto.ModelDTO;

/**
 * REST controller for managing models.
 */
@RestController
@RequestMapping("/api/mymodel")
@Transactional
public class MyModelResource {

	private static final Logger LOG = LoggerFactory.getLogger(MyModelResource.class);

	private static final String ENTITY_NAME = "model";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final ModelRepository modelRepository;

	public MyModelResource(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * {@code GET  /models} : get all the active models.
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of models in body.
	 */
	@GetMapping("getAllActiveModels")
	public List<ModelDTO> getAllActiveModels() {
		LOG.debug("REST request to get all active Models");
		var models = modelRepository.findAllByActiveTrue();
		return models.stream().map(model -> new ModelDTO(model.getId(), model.getModelName())).collect(Collectors.toList());
	}
}
