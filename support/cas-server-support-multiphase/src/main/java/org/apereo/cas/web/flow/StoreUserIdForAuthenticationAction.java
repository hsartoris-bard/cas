package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link StoreUserIdForAuthenticationAction}.
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class StoreUserIdForAuthenticationAction extends AbstractAction {
    //private final MultiphaseUserEventResolver multiphaseUserEventResolver;
    
    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event doExecute(final RequestContext requestContext) {
        val username = requestContext.getRequestParameters().get("username");

        if (StringUtils.equals(username, "testuser")) {
            requestContext.getFlowScope().put("redirectUrl", "https://www.google.com");
            return new EventFactorySupport().event(this, "multiphaseRedirectUserToAuthenticate");

        }
        /*
        if (StringUtils.isBlank(username)) {
            val message = new MessageBuilder().error().code("TODO").build();
            messageContext.addMessage(message);
            return error();
        }
        */
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        WebUtils.putMultiphaseAuthenticationUsername(requestContext, username);
        return success();
    }
}
