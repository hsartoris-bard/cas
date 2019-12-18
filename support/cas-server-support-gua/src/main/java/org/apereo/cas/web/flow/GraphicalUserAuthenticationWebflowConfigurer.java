package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GraphicalUserAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GraphicalUserAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Transition to obtain username.
     */
    public static final String TRANSITION_ID_GUA_GET_USERID = "guaGetUserId";

    static final String STATE_ID_ACCEPT_GUA = "acceptUserGraphicsForAuthentication";
    static final String STATE_ID_GUA_GET_USERID = "guaGetUserIdView";
    static final String STATE_ID_GUA_DISPLAY_USER_GFX = "guaDisplayUserGraphics";
    static final String ACTION_ID_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION = "displayUserGraphicsBeforeAuthenticationAction";

    static final String ACTION_ID_ACCEPT_USER_GRAPHICS_FOR_AUTHENTICATION = "acceptUserGraphicsForAuthenticationAction";

    public GraphicalUserAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                        final ApplicationContext applicationContext,
                                                        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
			// `state` is init; i.e., before display
            val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
			// retrieve Transition that will fire on success from init
            val transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
			// retrieve target of previous Transition, for use later
            val targetStateId = transition.getTargetStateId();
			// inject transition to guaGetUserId state
            createTransitionForState(state, TRANSITION_ID_GUA_GET_USERID, STATE_ID_GUA_GET_USERID);

			// create the state targeted by previous transition
            val viewState = createViewState(flow, STATE_ID_GUA_GET_USERID, "casGuaGetUserIdView");
			// attach transition to guaGetUserId state: on submit, move to
			// display user graphics state
			//
			// BUT ACTUALLY WE'RE OVERRIDING THAT
			// this means we simply go to the end state, without making the
			// user confirm their image
			//
			// in addition, DisplayUserGraphicsBeforeAuthenticationAction is
			// extended to perform the addition of a credential to the webflow,
			// namely UsernamePasswordCredential(username, null).
			// this is what AcceptUserGraphicsForAuthenticationAction normally
			// does. This also means that our standard login form needs to be
			// able to 1) take a preset username and 2) display a given image,
			// as would casGuaDisplayUserGraphicsView, but inline with regular
			// auth.
			createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, targetStateId);
            //createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_GUA_DISPLAY_USER_GFX);
            viewState.getRenderActionList().add(createEvaluateAction(ACTION_ID_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION));

			/*
            val viewStateGfx = createViewState(flow, STATE_ID_GUA_DISPLAY_USER_GFX, "casGuaDisplayUserGraphicsView");
            viewStateGfx.getRenderActionList().add(createEvaluateAction(ACTION_ID_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION));
            createTransitionForState(viewStateGfx, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_ACCEPT_GUA);

            val acceptState = createActionState(flow, STATE_ID_ACCEPT_GUA,
                createEvaluateAction(ACTION_ID_ACCEPT_USER_GRAPHICS_FOR_AUTHENTICATION));
            createStateDefaultTransition(acceptState, targetStateId);
			*/
        }
    }
}
