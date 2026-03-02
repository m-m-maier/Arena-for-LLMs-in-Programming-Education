package uibk.llmape.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import uibk.llmape.domain.enumeration.Category;

/**
 * A LeaderboardEntry.
 */
@Entity
@Table(name = "leaderboard_entry")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LeaderboardEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
	@SequenceGenerator(name = "sequenceGenerator")
	@Column(name = "id")
	private Long id;

	@NotNull
	@Size(max = 8192)
	@Column(name = "entry_json", length = 8192, nullable = false)
	private String entryJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private Category category;

	@NotNull
	@Column(name = "timestamp", nullable = false)
	private Instant timestamp;

	// jhipster-needle-entity-add-field - JHipster will add fields here

	public Long getId() {
		return this.id;
	}

	public LeaderboardEntry id(Long id) {
		this.setId(id);
		return this;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEntryJson() {
		return this.entryJson;
	}

	public LeaderboardEntry entryJson(String entryJson) {
		this.setEntryJson(entryJson);
		return this;
	}

	public void setEntryJson(String entryJson) {
		this.entryJson = entryJson;
	}

	public Category getCategory() {
		return this.category;
	}

	public LeaderboardEntry category(Category category) {
		this.setCategory(category);
		return this;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Instant getTimestamp() {
		return this.timestamp;
	}

	public LeaderboardEntry timestamp(Instant timestamp) {
		this.setTimestamp(timestamp);
		return this;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LeaderboardEntry)) {
			return false;
		}
		return getId() != null && getId().equals(((LeaderboardEntry) o).getId());
	}

	@Override
	public int hashCode() {
		// see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
		return getClass().hashCode();
	}

	// prettier-ignore
    @Override
    public String toString() {
        return "LeaderboardEntry{" +
            "id=" + getId() +
            ", entryJson='" + getEntryJson() + "'" +
            ", category='" + getCategory() + "'" +
            ", timestamp='" + getTimestamp() + "'" +
            "}";
    }
}
