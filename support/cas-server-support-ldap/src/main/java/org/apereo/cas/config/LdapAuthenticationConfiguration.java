package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.DefaultLdapAccountStateHandler;
import org.apereo.cas.authentication.support.OptionalWarningLdapAccountStateHandler;
import org.apereo.cas.authentication.support.RejectResultCodeLdapPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPasswordPolicyProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link LdapAuthenticationConfiguration} that attempts to create
 * relevant authentication handlers for LDAP.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class LdapAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    private static PasswordPolicyContext createLdapPasswordPolicyConfiguration(final LdapPasswordPolicyProperties passwordPolicy,
                                                                               final Authenticator authenticator,
                                                                               final Multimap<String, Object> attributes) {
        val cfg = new PasswordPolicyContext(passwordPolicy);
        val handlers = new HashSet<Object>();

        val customPolicyClass = passwordPolicy.getCustomPolicyClass();
        if (StringUtils.isNotBlank(customPolicyClass)) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password policy handler [{}]", customPolicyClass);
                val clazz = (Class<AuthenticationResponseHandler>) Class.forName(customPolicyClass);
                handlers.add(clazz.getDeclaredConstructor().newInstance());
            } catch (final Exception e) {
                LOGGER.warn("Unable to construct an instance of the password policy handler", e);
            }
        }
        LOGGER.debug("Password policy authentication response handler is set to accommodate directory type: [{}]", passwordPolicy.getType());
        switch (passwordPolicy.getType()) {
            case AD:
                handlers.add(new ActiveDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
                Arrays.stream(ActiveDirectoryAuthenticationResponseHandler.ATTRIBUTES).forEach(a -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", a);
                    attributes.put(a, a);
                });
                break;
            case FreeIPA:
                Arrays.stream(FreeIPAAuthenticationResponseHandler.ATTRIBUTES).forEach(a -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", a);
                    attributes.put(a, a);
                });
                handlers.add(new FreeIPAAuthenticationResponseHandler(
                    Period.ofDays(cfg.getPasswordWarningNumberOfDays()), cfg.getLoginFailures()));
                break;
            case EDirectory:
                Arrays.stream(EDirectoryAuthenticationResponseHandler.ATTRIBUTES).forEach(a -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", a);
                    attributes.put(a, a);
                });
                handlers.add(new EDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
                break;
            default:
                handlers.add(new PasswordPolicyAuthenticationResponseHandler());
                handlers.add(new PasswordExpirationAuthenticationResponseHandler());
                break;
        }
        authenticator.setResponseHandlers(handlers.toArray(AuthenticationResponseHandler[]::new));

        LOGGER.debug("LDAP authentication response handlers configured are: [{}]", handlers);

        if (!passwordPolicy.isAccountStateHandlingEnabled()) {
            cfg.setAccountStateHandler((response, configuration) -> new ArrayList<>(0));
            LOGGER.debug("Handling LDAP account states is disabled via CAS configuration");
        } else if (StringUtils.isNotBlank(passwordPolicy.getWarningAttributeName()) && StringUtils.isNotBlank(passwordPolicy.getWarningAttributeValue())) {
            val accountHandler = new OptionalWarningLdapAccountStateHandler();
            accountHandler.setDisplayWarningOnMatch(passwordPolicy.isDisplayWarningOnMatch());
            accountHandler.setWarnAttributeName(passwordPolicy.getWarningAttributeName());
            accountHandler.setWarningAttributeValue(passwordPolicy.getWarningAttributeValue());
            accountHandler.setAttributesToErrorMap(passwordPolicy.getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring an warning account state handler for LDAP authentication for warning attribute [{}] and value [{}]",
                passwordPolicy.getWarningAttributeName(), passwordPolicy.getWarningAttributeValue());
        } else {
            val accountHandler = new DefaultLdapAccountStateHandler();
            accountHandler.setAttributesToErrorMap(passwordPolicy.getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring the default account state handler for LDAP authentication");
        }
        return cfg;
    }

    @ConditionalOnMissingBean(name = "ldapPrincipalFactory")
    @Bean
    public PrincipalFactory ldapPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    public Collection<AuthenticationHandler> ldapAuthenticationHandlers() {
        val handlers = new HashSet<AuthenticationHandler>();

        casProperties.getAuthn().getLdap()
            .stream()
            .filter(l -> {
                if (l.getType() == null) {
                    LOGGER.warn("Skipping LDAP authentication entry since no type is defined");
                    return false;
                }
                if (StringUtils.isBlank(l.getLdapUrl())) {
                    LOGGER.warn("Skipping LDAP authentication entry since no LDAP url is defined");
                    return false;
                }
                return true;
            })
            .forEach(l -> {
                val multiMapAttributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(l.getPrincipalAttributeList());
                LOGGER.debug("Created and mapped principal attributes [{}] for [{}]...", multiMapAttributes, l.getLdapUrl());

                LOGGER.debug("Creating LDAP authenticator for [{}] and baseDn [{}]", l.getLdapUrl(), l.getBaseDn());
                val authenticator = LdapUtils.newLdaptiveAuthenticator(l);
                LOGGER.debug("Ldap authenticator configured with return attributes [{}] for [{}] and baseDn [{}]",
                    multiMapAttributes.keySet(), l.getLdapUrl(), l.getBaseDn());

                LOGGER.debug("Creating LDAP password policy handling strategy for [{}]", l.getLdapUrl());
                val strategy = createLdapPasswordPolicyHandlingStrategy(l);

                LOGGER.debug("Creating LDAP authentication handler for [{}]", l.getLdapUrl());
                val handler = new LdapAuthenticationHandler(l.getName(),
                    servicesManager.getObject(), ldapPrincipalFactory(),
                    l.getOrder(), authenticator, strategy);
                handler.setCollectDnAttribute(l.isCollectDnAttribute());

                if (StringUtils.isNotBlank(l.getPrincipalAttributeId())) {
                    val additionalAttributes = l.getAdditionalAttributes();
                    additionalAttributes.add(l.getPrincipalAttributeId());
                }
                if (StringUtils.isNotBlank(l.getPrincipalDnAttributeName())) {
                    handler.setPrincipalDnAttributeName(l.getPrincipalDnAttributeName());
                }
                handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());
                handler.setAllowMissingPrincipalAttributeValue(l.isAllowMissingPrincipalAttributeValue());
                handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(l.getPasswordEncoder(), applicationContext));
                handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(l.getPrincipalTransformation()));

                if (StringUtils.isNotBlank(l.getCredentialCriteria())) {
                    LOGGER.debug("Ldap authentication for [{}] is filtering credentials by [{}]",
                        l.getLdapUrl(), l.getCredentialCriteria());
                    handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(l.getCredentialCriteria()));
                }

                if (StringUtils.isBlank(l.getPrincipalAttributeId())) {
                    LOGGER.debug("No principal id attribute is found for LDAP authentication via [{}]", l.getLdapUrl());
                } else {
                    handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());
                    LOGGER.debug("Using principal id attribute [{}] for LDAP authentication via [{}]", l.getPrincipalAttributeId(),
                        l.getLdapUrl());
                }

                val passwordPolicy = l.getPasswordPolicy();
                if (passwordPolicy.isEnabled()) {
                    LOGGER.debug("Password policy is enabled for [{}]. Constructing password policy configuration", l.getLdapUrl());
                    val cfg = createLdapPasswordPolicyConfiguration(passwordPolicy, authenticator, multiMapAttributes);
                    handler.setPasswordPolicyConfiguration(cfg);
                }

                val attributes = CollectionUtils.wrap(multiMapAttributes);
                handler.setPrincipalAttributeMap(attributes);

                LOGGER.debug("Initializing LDAP authentication handler for [{}]", l.getLdapUrl());
                handler.initialize();
                handlers.add(handler);
            });
        return handlers;
    }

    private AuthenticationPasswordPolicyHandlingStrategy<AuthenticationResponse, PasswordPolicyContext>
        createLdapPasswordPolicyHandlingStrategy(final LdapAuthenticationProperties l) {
        if (l.getPasswordPolicy().getStrategy() == LdapPasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE) {
            LOGGER.debug("Created LDAP password policy handling strategy based on blacklisted authentication result codes");
            return new RejectResultCodeLdapPasswordPolicyHandlingStrategy();
        }

        val location = l.getPasswordPolicy().getGroovy().getLocation();
        if (l.getPasswordPolicy().getStrategy() == LdapPasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY && location != null) {
            LOGGER.debug("Created LDAP password policy handling strategy based on Groovy script [{}]", location);
            return new GroovyPasswordPolicyHandlingStrategy(location, applicationContext);
        }

        LOGGER.debug("Created default LDAP password policy handling strategy");
        return new DefaultPasswordPolicyHandlingStrategy();
    }

    @ConditionalOnMissingBean(name = "ldapAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer ldapAuthenticationEventExecutionPlanConfigurer() {
        return plan -> ldapAuthenticationHandlers().forEach(handler -> {
            LOGGER.info("Registering LDAP authentication for [{}]", handler.getName());
            plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver.getObject());
        });
    }
}
