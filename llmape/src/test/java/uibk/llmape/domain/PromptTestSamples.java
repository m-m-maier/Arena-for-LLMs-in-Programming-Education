package uibk.llmape.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PromptTestSamples {

	private static final Random random = new Random();
	private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	public static Prompt getPromptSample1() {
		return new Prompt().id(1L).promptText("promptText1").sessionId("sessionId1").generationModelId(1L);
	}

	public static Prompt getPromptSample2() {
		return new Prompt().id(2L).promptText("promptText2").sessionId("sessionId2").generationModelId(2L);
	}

	public static Prompt getPromptRandomSampleGenerator() {
		return new Prompt()
			.id(longCount.incrementAndGet())
			.promptText(UUID.randomUUID().toString())
			.sessionId(UUID.randomUUID().toString())
			.generationModelId(longCount.incrementAndGet());
	}
}
