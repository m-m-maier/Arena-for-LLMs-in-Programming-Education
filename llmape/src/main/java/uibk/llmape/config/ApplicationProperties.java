package uibk.llmape.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Llmape.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

	private final Liquibase liquibase = new Liquibase();
    private final RejectionModel rejectionModel = new RejectionModel();
    private final Gmail gmail = new Gmail();
    private final RateLimit rateLimit = new RateLimit();


	// jhipster-needle-application-properties-property

	public Liquibase getLiquibase() {
		return liquibase;
	}
    public RejectionModel getRejectionModel() { return rejectionModel; }
    public Gmail getGmail(){ return gmail; }
    public RateLimit getRateLimit(){ return rateLimit; }

	// jhipster-needle-application-properties-property-getter

	public static class Liquibase {

		private Boolean asyncStart = true;

		public Boolean getAsyncStart() {
			return asyncStart;
		}

		public void setAsyncStart(Boolean asyncStart) {
			this.asyncStart = asyncStart;
		}
	}

    public static class RejectionModel {
        private String modelName;
        private String token;
        private String baseurl = "";
        private String provider;

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getBaseurl() {
            return baseurl;
        }

        public void setBaseurl(String baseurl) {
            this.baseurl = baseurl;
        }

        public String getProvider() { return provider; }

        public void setProvider(String provider) { this.provider = provider; }
    }

    public static class Gmail{
        private String clientId;
        private String clientSecret;
        private String refreshToken;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    public static class RateLimit{

        private int global;
        private int perIp;

        public int getGlobal() {
            return global;
        }

        public void setGlobal(int global) {
            this.global = global;
        }

        public int getPerIp() {
            return perIp;
        }

        public void setPerIp(int perIp) {
            this.perIp = perIp;
        }
    }
	// jhipster-needle-application-properties-property-class
}
