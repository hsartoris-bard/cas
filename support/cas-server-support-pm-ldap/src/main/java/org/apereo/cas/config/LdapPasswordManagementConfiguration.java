package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.LdapPasswordManagementService;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapPasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "ldapPasswordManagementConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty("cas.authn.pm.ldap[0].ldapUrl")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapPasswordManagementConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private ObjectProvider<CipherExecutor> passwordManagementCipherExecutor;

    @Autowired
    @Qualifier("passwordHistoryService")
    private ObjectProvider<PasswordHistoryService> passwordHistoryService;

    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        return new LdapPasswordManagementService(passwordManagementCipherExecutor.getObject(),
            casProperties.getServer().getPrefix(),
            casProperties.getAuthn().getPm(),
            passwordHistoryService.getObject());
    }
}
