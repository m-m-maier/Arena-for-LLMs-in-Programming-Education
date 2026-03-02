package uibk.llmape.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Battle.
 */
@Entity
@Table(name = "battle")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Battle implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
	@SequenceGenerator(name = "sequenceGenerator")
	@Column(name = "id")
	private Long id;

	@Size(max = 16320)
	@Column(name = "model_1_answer", length = 16320)
	private String model1Answer;

	@Size(max = 16320)
	@Column(name = "model_2_answer", length = 16320)
	private String model2Answer;

	@Column(name = "vote_timestamp")
	private Instant voteTimestamp;

	@JsonIgnoreProperties(value = { "battle" }, allowSetters = true)
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(unique = true)
	private Prompt prompt;

	@ManyToOne(fetch = FetchType.LAZY)
	private Model model1;

	@ManyToOne(fetch = FetchType.LAZY)
	private Model model2;

	@ManyToOne(fetch = FetchType.LAZY)
	private Model winnerModel;

	// jhipster-needle-entity-add-field - JHipster will add fields here

	public Long getId() {
		return this.id;
	}

	public Battle id(Long id) {
		this.setId(id);
		return this;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModel1Answer() {
		return this.model1Answer;
	}

	public Battle model1Answer(String model1Answer) {
		this.setModel1Answer(model1Answer);
		return this;
	}

	public void setModel1Answer(String model1Answer) {
		this.model1Answer = model1Answer;
	}

	public String getModel2Answer() {
		return this.model2Answer;
	}

	public Battle model2Answer(String model2Answer) {
		this.setModel2Answer(model2Answer);
		return this;
	}

	public void setModel2Answer(String model2Answer) {
		this.model2Answer = model2Answer;
	}

	public Instant getVoteTimestamp() {
		return this.voteTimestamp;
	}

	public Battle voteTimestamp(Instant voteTimestamp) {
		this.setVoteTimestamp(voteTimestamp);
		return this;
	}

	public void setVoteTimestamp(Instant voteTimestamp) {
		this.voteTimestamp = voteTimestamp;
	}

	public Prompt getPrompt() {
		return this.prompt;
	}

	public void setPrompt(Prompt prompt) {
		this.prompt = prompt;
	}

	public Battle prompt(Prompt prompt) {
		this.setPrompt(prompt);
		return this;
	}

	public Model getModel1() {
		return this.model1;
	}

	public void setModel1(Model model) {
		this.model1 = model;
	}

	public Battle model1(Model model) {
		this.setModel1(model);
		return this;
	}

	public Model getModel2() {
		return this.model2;
	}

	public void setModel2(Model model) {
		this.model2 = model;
	}

	public Battle model2(Model model) {
		this.setModel2(model);
		return this;
	}

	public Model getWinnerModel() {
		return this.winnerModel;
	}

	public void setWinnerModel(Model model) {
		this.winnerModel = model;
	}

	public Battle winnerModel(Model model) {
		this.setWinnerModel(model);
		return this;
	}

	// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Battle)) {
			return false;
		}
		return getId() != null && getId().equals(((Battle) o).getId());
	}

	@Override
	public int hashCode() {
		// see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
		return getClass().hashCode();
	}

	// prettier-ignore
    @Override
    public String toString() {
        return "Battle{" +
            "id=" + getId() +
            ", model1Answer='" + getModel1Answer() + "'" +
            ", model2Answer='" + getModel2Answer() + "'" +
            ", voteTimestamp='" + getVoteTimestamp() + "'" +
            "}";
    }
}
