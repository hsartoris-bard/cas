package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * Updates webflow to insert {@link PrincipalProvisionerAction} into flow after TGT created.
 * Very slight modification from original {@see ScimWebflowConfigurer} by {@author Misagh Moayyed}.
 *
 * @author Hayden Sartoris
 * @since 6.3.0
 */

public class ProvisioningWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public ProvisioningWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                         final ConfigurableApplicationContext applicationContext,
                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val tgtAction = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
            tgtAction.getExitActionList().add(createEvaluateAction("principalProvisionerAction"));
        }
    }
}

