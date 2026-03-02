package uibk.llmape.service.llm;

import java.util.Objects;
import org.springframework.lang.Nullable;

public class ModelKey {

	private final String provider;
	private final String modelName;

	public ModelKey(String provider, String modelName) {
		this.provider = provider;
		this.modelName = modelName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ModelKey modelKey = (ModelKey) o;
		return provider.equals(modelKey.provider) && modelName.equals(modelKey.modelName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(provider, modelName);
	}
}
