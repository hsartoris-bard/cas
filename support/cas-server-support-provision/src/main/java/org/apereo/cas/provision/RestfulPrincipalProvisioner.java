package org.apereo.cas.provision;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

/**
 * Defines a basic {@link PrincipalProvisioner} over HTTP.
 *
 * @author Hayden Sartoris
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulPrincipalProvisioner implements PrincipalProvisioner {
    @NonNull
    private ObjectMapper objectMapper;

    @NonNull
    private final String url;

    @NonNull
    private final String method;

    private final String username;
    private final String password;

    @Override
    public boolean create(Authentication auth, Principal p, Credential cred) {
        val headers = new HashMap<String, Object>();
        headers.put("id", p.getId());
        headers.put("attributes", p.getAttributes());


        HttpResponse response = null;
        try {
            val body = objectMapper.writeValueAsString(p);
            response = HttpUtils.execute(url, method, username, password, new HashMap<>(0), headers, body, null);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    LOGGER.debug("Provisioned principal with id [{}] successfully (status code [{}])",
                            p.getId(), status);
                    return true;
                } else {
                    LOGGER.warn("Failed to provision principal [{}]; response: [{}]", p, response);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

}


