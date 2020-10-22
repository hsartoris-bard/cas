package org.apereo.cas.config;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.provision.RestfulPrincipalProvisioner;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.PrincipalProvisionerAction;
import org.apereo.cas.web.flow.ProvisioningWebflowConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasProvisioningConfiguration}, providing basic principal provisioning capabilities.
 *
 * @author Hayden Sartoris
 * @since 6.3.0
 */

@Configuration("casProvisioningConfiguration")
public class CasProvisioningConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<ObjectMapper> jacksonObjectMapper;

    //@ConditionalOnProperty(name = "cas.provisioning.rest")
    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "principalProvisioner")
    public PrincipalProvisioner principalProvisioner() {
        val rest = casProperties.getProvisioning().getRest();
        // TODO
        return new RestfulPrincipalProvisioner(jacksonObjectMapper.getObject(),
                                               rest.getUrl(), 
                                               rest.getMethod(), 
                                               rest.getBasicAuthUsername(), 
                                               rest.getBasicAuthPassword());
    }

    @ConditionalOnMissingBean(name = "principalProvisionerAction")
    @Bean
    @RefreshScope
    public Action principalProvisionerAction() {
        return new PrincipalProvisionerAction(principalProvisioner());
    }

    @ConditionalOnMissingBean(name = "provisioningWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer provisioningWebflowConfigurer() {
        return new ProvisioningWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "provisioningCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer provisioningCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(provisioningWebflowConfigurer());
    }
}

