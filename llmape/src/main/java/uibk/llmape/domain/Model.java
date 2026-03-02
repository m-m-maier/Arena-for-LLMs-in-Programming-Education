package uibk.llmape.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Model.
 */
@Entity
@Table(name = "model")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Model implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
	@SequenceGenerator(name = "sequenceGenerator")
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name = "model_name", nullable = false)
	private String modelName;

	@NotNull
	@Column(name = "organization", nullable = false)
	private String organization;

	@NotNull
	@Column(name = "provider", nullable = false)
	private String provider;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "base_url")
	private String baseUrl;

	@NotNull
	@Column(name = "license", nullable = false)
	private String license;

	@NotNull
	@Column(name = "active", nullable = false)
	private Boolean active;

	// jhipster-needle-entity-add-field - JHipster will add fields here

	public Long getId() {
		return this.id;
	}

	public Model id(Long id) {
		this.setId(id);
		return this;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModelName() {
		return this.modelName;
	}

	public Model modelName(String modelName) {
		this.setModelName(modelName);
		return this;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getOrganization() {
		return this.organization;
	}

	public Model organization(String organization) {
		this.setOrganization(organization);
		return this;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getProvider() {
		return this.provider;
	}

	public Model provider(String provider) {
		this.setProvider(provider);
		return this;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public Model apiKey(String apiKey) {
		this.setApiKey(apiKey);
		return this;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public Model baseUrl(String baseUrl) {
		this.setBaseUrl(baseUrl);
		return this;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getLicense() {
		return this.license;
	}

	public Model license(String license) {
		this.setLicense(license);
		return this;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public Boolean getActive() {
		return this.active;
	}

	public Model active(Boolean active) {
		this.setActive(active);
		return this;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Model)) {
			return false;
		}
		return getId() != null && getId().equals(((Model) o).getId());
	}

	@Override
	public int hashCode() {
		// see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
		return getClass().hashCode();
	}

	// prettier-ignore
    @Override
    public String toString() {
        return "Model{" +
            "id=" + getId() +
            ", modelName='" + getModelName() + "'" +
            ", organization='" + getOrganization() + "'" +
            ", provider='" + getProvider() + "'" +
            ", apiKey='" + getApiKey() + "'" +
            ", baseUrl='" + getBaseUrl() + "'" +
            ", license='" + getLicense() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
