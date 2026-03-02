package uibk.llmape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uibk.llmape.domain.ModelTestSamples.*;

import org.junit.jupiter.api.Test;
import uibk.llmape.web.rest.TestUtil;

class ModelTest {

	@Test
	void equalsVerifier() throws Exception {
		TestUtil.equalsVerifier(Model.class);
		Model model1 = getModelSample1();
		Model model2 = new Model();
		assertThat(model1).isNotEqualTo(model2);

		model2.setId(model1.getId());
		assertThat(model1).isEqualTo(model2);

		model2 = getModelSample2();
		assertThat(model1).isNotEqualTo(model2);
	}
}
