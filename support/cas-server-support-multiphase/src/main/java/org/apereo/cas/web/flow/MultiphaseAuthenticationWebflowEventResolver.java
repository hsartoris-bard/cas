package org.apereo.cas.web.flow;

//import org.apereo.cas.web.flow.authentication.
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link MultiphaseAuthenticationWebflowEventResolver}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Slf4j
public class MultiphaseAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    public MultiphaseAuthenticationWebflowEventResolver(
            final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext requestContext) {
        // TODO
        return null;
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
