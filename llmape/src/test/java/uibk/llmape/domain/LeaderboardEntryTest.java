package uibk.llmape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uibk.llmape.domain.LeaderboardEntryTestSamples.*;

import org.junit.jupiter.api.Test;
import uibk.llmape.web.rest.TestUtil;

class LeaderboardEntryTest {

	@Test
	void equalsVerifier() throws Exception {
		TestUtil.equalsVerifier(LeaderboardEntry.class);
		LeaderboardEntry leaderboardEntry1 = getLeaderboardEntrySample1();
		LeaderboardEntry leaderboardEntry2 = new LeaderboardEntry();
		assertThat(leaderboardEntry1).isNotEqualTo(leaderboardEntry2);

		leaderboardEntry2.setId(leaderboardEntry1.getId());
		assertThat(leaderboardEntry1).isEqualTo(leaderboardEntry2);

		leaderboardEntry2 = getLeaderboardEntrySample2();
		assertThat(leaderboardEntry1).isNotEqualTo(leaderboardEntry2);
	}
}
