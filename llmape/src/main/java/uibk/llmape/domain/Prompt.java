package uibk.llmape.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import uibk.llmape.domain.enumeration.Category;

/**
 * A Prompt.
 */
@Entity
@Table(name = "prompt")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Prompt implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
	@SequenceGenerator(name = "sequenceGenerator")
	@Column(name = "id")
	private Long id;

	@NotNull
	@Size(max = 16320)
	@Column(name = "prompt_text", length = 16320, nullable = false)
	private String promptText;

	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private Category category;

	@NotNull
	@Column(name = "is_rejected", nullable = false)
	private Boolean isRejected;

	@NotNull
	@Column(name = "is_from_public_page", nullable = false)
	private Boolean isFromPublicPage;

	@NotNull
	@Column(name = "timestamp", nullable = false)
	private Instant timestamp;

	@NotNull
	@Column(name = "session_id", nullable = false)
	private String sessionId;

	@Column(name = "generation_model_id")
	private Long generationModelId;

	@JsonIgnoreProperties(value = { "prompt", "model1", "model2", "winnerModel" }, allowSetters = true)
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "prompt")
	private Battle battle;

	// jhipster-needle-entity-add-field - JHipster will add fields here

	public Long getId() {
		return this.id;
	}

	public Prompt id(Long id) {
		this.setId(id);
		return this;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPromptText() {
		return this.promptText;
	}

	public Prompt promptText(String promptText) {
		this.setPromptText(promptText);
		return this;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public Category getCategory() {
		return this.category;
	}

	public Prompt category(Category category) {
		this.setCategory(category);
		return this;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Boolean getIsRejected() {
		return this.isRejected;
	}

	public Prompt isRejected(Boolean isRejected) {
		this.setIsRejected(isRejected);
		return this;
	}

	public void setIsRejected(Boolean isRejected) {
		this.isRejected = isRejected;
	}

	public Boolean getIsFromPublicPage() {
		return this.isFromPublicPage;
	}

	public Prompt isFromPublicPage(Boolean isFromPublicPage) {
		this.setIsFromPublicPage(isFromPublicPage);
		return this;
	}

	public void setIsFromPublicPage(Boolean isFromPublicPage) {
		this.isFromPublicPage = isFromPublicPage;
	}

	public Instant getTimestamp() {
		return this.timestamp;
	}

	public Prompt timestamp(Instant timestamp) {
		this.setTimestamp(timestamp);
		return this;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public Prompt sessionId(String sessionId) {
		this.setSessionId(sessionId);
		return this;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Long getGenerationModelId() {
		return this.generationModelId;
	}

	public Prompt generationModelId(Long generationModelId) {
		this.setGenerationModelId(generationModelId);
		return this;
	}

	public void setGenerationModelId(Long generationModelId) {
		this.generationModelId = generationModelId;
	}

	public Battle getBattle() {
		return this.battle;
	}

	public void setBattle(Battle battle) {
		if (this.battle != null) {
			this.battle.setPrompt(null);
		}
		if (battle != null) {
			battle.setPrompt(this);
		}
		this.battle = battle;
	}

	public Prompt battle(Battle battle) {
		this.setBattle(battle);
		return this;
	}

	// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Prompt)) {
			return false;
		}
		return getId() != null && getId().equals(((Prompt) o).getId());
	}

	@Override
	public int hashCode() {
		// see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
		return getClass().hashCode();
	}

	// prettier-ignore
    @Override
    public String toString() {
        return "Prompt{" +
            "id=" + getId() +
            ", promptText='" + getPromptText() + "'" +
            ", category='" + getCategory() + "'" +
            ", isRejected='" + getIsRejected() + "'" +
            ", isFromPublicPage='" + getIsFromPublicPage() + "'" +
            ", timestamp='" + getTimestamp() + "'" +
            ", sessionId='" + getSessionId() + "'" +
            ", generationModelId=" + getGenerationModelId() +
            "}";
    }
}
