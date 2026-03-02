package uibk.llmape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uibk.llmape.domain.BattleTestSamples.*;
import static uibk.llmape.domain.ModelTestSamples.*;
import static uibk.llmape.domain.PromptTestSamples.*;

import org.junit.jupiter.api.Test;
import uibk.llmape.web.rest.TestUtil;

class BattleTest {

	@Test
	void equalsVerifier() throws Exception {
		TestUtil.equalsVerifier(Battle.class);
		Battle battle1 = getBattleSample1();
		Battle battle2 = new Battle();
		assertThat(battle1).isNotEqualTo(battle2);

		battle2.setId(battle1.getId());
		assertThat(battle1).isEqualTo(battle2);

		battle2 = getBattleSample2();
		assertThat(battle1).isNotEqualTo(battle2);
	}

	@Test
	void promptTest() {
		Battle battle = getBattleRandomSampleGenerator();
		Prompt promptBack = getPromptRandomSampleGenerator();

		battle.setPrompt(promptBack);
		assertThat(battle.getPrompt()).isEqualTo(promptBack);

		battle.prompt(null);
		assertThat(battle.getPrompt()).isNull();
	}

	@Test
	void model1Test() {
		Battle battle = getBattleRandomSampleGenerator();
		Model modelBack = getModelRandomSampleGenerator();

		battle.setModel1(modelBack);
		assertThat(battle.getModel1()).isEqualTo(modelBack);

		battle.model1(null);
		assertThat(battle.getModel1()).isNull();
	}

	@Test
	void model2Test() {
		Battle battle = getBattleRandomSampleGenerator();
		Model modelBack = getModelRandomSampleGenerator();

		battle.setModel2(modelBack);
		assertThat(battle.getModel2()).isEqualTo(modelBack);

		battle.model2(null);
		assertThat(battle.getModel2()).isNull();
	}

	@Test
	void winnerModelTest() {
		Battle battle = getBattleRandomSampleGenerator();
		Model modelBack = getModelRandomSampleGenerator();

		battle.setWinnerModel(modelBack);
		assertThat(battle.getWinnerModel()).isEqualTo(modelBack);

		battle.winnerModel(null);
		assertThat(battle.getWinnerModel()).isNull();
	}
}
