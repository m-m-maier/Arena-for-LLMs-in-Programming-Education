package uibk.llmape.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ModelTestSamples {

	private static final Random random = new Random();
	private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

	public static Model getModelSample1() {
		return new Model()
			.id(1L)
			.modelName("modelName1")
			.organization("organization1")
			.provider("provider1")
			.apiKey("apiKey1")
			.baseUrl("baseUrl1")
			.license("license1");
	}

	public static Model getModelSample2() {
		return new Model()
			.id(2L)
			.modelName("modelName2")
			.organization("organization2")
			.provider("provider2")
			.apiKey("apiKey2")
			.baseUrl("baseUrl2")
			.license("license2");
	}

	public static Model getModelRandomSampleGenerator() {
		return new Model()
			.id(longCount.incrementAndGet())
			.modelName(UUID.randomUUID().toString())
			.organization(UUID.randomUUID().toString())
			.provider(UUID.randomUUID().toString())
			.apiKey(UUID.randomUUID().toString())
			.baseUrl(UUID.randomUUID().toString())
			.license(UUID.randomUUID().toString());
	}
}
