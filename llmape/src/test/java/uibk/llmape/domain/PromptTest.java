package uibk.llmape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uibk.llmape.domain.BattleTestSamples.*;
import static uibk.llmape.domain.PromptTestSamples.*;

import org.junit.jupiter.api.Test;
import uibk.llmape.web.rest.TestUtil;

class PromptTest {

	@Test
	void equalsVerifier() throws Exception {
		TestUtil.equalsVerifier(Prompt.class);
		Prompt prompt1 = getPromptSample1();
		Prompt prompt2 = new Prompt();
		assertThat(prompt1).isNotEqualTo(prompt2);

		prompt2.setId(prompt1.getId());
		assertThat(prompt1).isEqualTo(prompt2);

		prompt2 = getPromptSample2();
		assertThat(prompt1).isNotEqualTo(prompt2);
	}

	@Test
	void battleTest() {
		Prompt prompt = getPromptRandomSampleGenerator();
		Battle battleBack = getBattleRandomSampleGenerator();

		prompt.setBattle(battleBack);
		assertThat(prompt.getBattle()).isEqualTo(battleBack);
		assertThat(battleBack.getPrompt()).isEqualTo(prompt);

		prompt.battle(null);
		assertThat(prompt.getBattle()).isNull();
		assertThat(battleBack.getPrompt()).isNull();
	}
}
