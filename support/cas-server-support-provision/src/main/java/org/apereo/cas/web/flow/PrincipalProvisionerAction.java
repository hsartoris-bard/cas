package org.apereo.cas.web.flow;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Dispatches resolved principal externally for creation, if necessary.
 * This will be inserted into the webflow by {@see ProvisioningWebflowConfigurer}.
 * Internal logic largely identical to that of {@see PrincipalScimProvisionerAction} by {@author Misagh Moayyed}.
 *
 * @author Hayden Sartoris
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class PrincipalProvisionerAction extends AbstractAction {
    private final PrincipalProvisioner provisioner;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val c = WebUtils.getCredential(requestContext);
        if (c == null) {
            LOGGER.warn("No credential found in the request context to provision");
            return success();
        }
        val authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.warn("No authentication found in the request context to provision");
            return success();
        }
        val p = authentication.getPrincipal();
        LOGGER.debug("Starting to provision principal [{}]", p);
        val res = provisioner.create(authentication, p, c);
        if (res) {
            LOGGER.debug("Provisioning of principal [{}] executed successfully", p);
        } else {
            LOGGER.warn("Provisioning of principal [{}] has failed", p);
        }
        return success();
    }
}

