package uibk.llmape.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.stereotype.Component;
import uibk.llmape.config.ApplicationProperties;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Component
public class OAuth2TokenProvider {

    private final ApplicationProperties applicationProperties;

    private AccessToken currentToken;


    public OAuth2TokenProvider(ApplicationProperties applicationProperties){
        this.applicationProperties = applicationProperties;
    }

    public synchronized String getValidAccessToken() throws IOException {
        if(currentToken == null || currentToken.getExpirationTime().before(Date.from(Instant.now().plusSeconds(60)))){
            UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(applicationProperties.getGmail().getClientId())
                .setClientSecret(applicationProperties.getGmail().getClientSecret())
                .setRefreshToken(applicationProperties.getGmail().getRefreshToken())
                .build();

            credentials.refresh();
            currentToken = credentials.getAccessToken();
        }
        return currentToken.getTokenValue();
    }


}
