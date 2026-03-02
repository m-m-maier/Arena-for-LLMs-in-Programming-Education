package uibk.llmape.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BattleTestSamples {

	private static final Random random = new Random();
	private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	public static Battle getBattleSample1() {
		return new Battle().id(1L).model1Answer("model1Answer1").model2Answer("model2Answer1");
	}

	public static Battle getBattleSample2() {
		return new Battle().id(2L).model1Answer("model1Answer2").model2Answer("model2Answer2");
	}

	public static Battle getBattleRandomSampleGenerator() {
		return new Battle()
			.id(longCount.incrementAndGet())
			.model1Answer(UUID.randomUUID().toString())
			.model2Answer(UUID.randomUUID().toString());
	}
}
