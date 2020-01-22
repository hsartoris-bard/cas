package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link MultiphaseAuthenticationWebflowConfigurer}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Slf4j
public class MultiphaseAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Transition to obtain username.
     */
    public static final String TRANSITION_ID_MULTIPHASE_GET_USERID = "multiphaseGetUserId";

    static final String ACTION_ID_STORE_USERID_FOR_AUTHENTICATION = "storeUserIdForAuthenticationAction";
    static final String VIEW_ID_REDIRECT_USER = "redirectUserForAuthentication";

    public MultiphaseAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                     final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                     final ApplicationContext applicationContext,
                                                     final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val initState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);

            LOGGER.debug("Locating transition id [{}] for state [{}]", 
                    CasWebflowConstants.TRANSITION_ID_SUCCESS, initState.getId());
            val initTransition = (Transition) initState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            val targetStateId = initTransition.getTargetStateId();

            LOGGER.debug("Creating transition with id [{}] for state [{}] to state [{}]",
                    TRANSITION_ID_MULTIPHASE_GET_USERID, initState.getId(), 
                    CasWebflowConstants.VIEW_ID_MULTIPHASE_GET_USERID);
            createTransitionForState(initState, 
                    TRANSITION_ID_MULTIPHASE_GET_USERID, 
                    CasWebflowConstants.VIEW_ID_MULTIPHASE_GET_USERID);

            val getUserIdState = createViewState(flow, 
                    CasWebflowConstants.VIEW_ID_MULTIPHASE_GET_USERID, 
                    "casMultiphaseGetUserIdView");

            LOGGER.debug("Creating transition with id [{}] for state [{}] to state [{}]",
                    CasWebflowConstants.TRANSITION_ID_SUBMIT, getUserIdState.getId(), 
                    CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID);
            createTransitionForState(getUserIdState, 
                    CasWebflowConstants.TRANSITION_ID_SUBMIT, 
                    CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID);

            val actionState = createActionState(flow, 
                    CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID,
                    createEvaluateAction(ACTION_ID_STORE_USERID_FOR_AUTHENTICATION));

            val noRedirectTransition = createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, targetStateId);
            val yesRedirectTransition = createTransition("multiphaseRedirectUserToAuthenticate",
                    VIEW_ID_REDIRECT_USER);

            val transitionSet = actionState.getTransitionSet();
            transitionSet.add(noRedirectTransition);
            transitionSet.add(yesRedirectTransition);

            createViewState(flow, VIEW_ID_REDIRECT_USER, "casMultiphaseRedirectView");
            //val redirectView = createViewState(flow, VIEW_ID_REDIRECT_USER, "casMultiphaseRedirectView");
            
            LOGGER.debug("Creating transition with id [{}] for state [{}] to state [{}]",
                    CasWebflowConstants.TRANSITION_ID_SUCCESS, actionState.getId(), targetStateId);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetStateId);
        }
    }
}
