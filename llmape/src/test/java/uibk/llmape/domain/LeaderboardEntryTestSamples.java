package uibk.llmape.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class LeaderboardEntryTestSamples {

	private static final Random random = new Random();
	private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	public static LeaderboardEntry getLeaderboardEntrySample1() {
		return new LeaderboardEntry().id(1L).entryJson("entryJson1");
	}

	public static LeaderboardEntry getLeaderboardEntrySample2() {
		return new LeaderboardEntry().id(2L).entryJson("entryJson2");
	}

	public static LeaderboardEntry getLeaderboardEntryRandomSampleGenerator() {
		return new LeaderboardEntry().id(longCount.incrementAndGet()).entryJson(UUID.randomUUID().toString());
	}
}
