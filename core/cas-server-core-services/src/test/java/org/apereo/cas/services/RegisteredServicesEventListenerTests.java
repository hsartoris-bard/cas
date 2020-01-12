package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.util.MockSmsSender;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.util.io.SmsSender;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link RegisteredServicesEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    RegisteredServicesEventListenerTests.RegisteredServicesEventListenerTestConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
}, properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "cas.serviceRegistry.sms.text=Service %s has expired in CAS service registry",
    "cas.serviceRegistry.sms.from=3477563421",
    "cas.serviceRegistry.mail.from=admin@example.org",
    "cas.serviceRegistry.mail.subject=Sample Subject",
    "cas.serviceRegistry.mail.text=Service %s has expired in CAS service registry"
})
@Tag("Mail")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 25000)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RegisteredServicesEventListenerTests {
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyServiceExpirationEvent() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val contact = new DefaultRegisteredServiceContact();
        contact.setName("Test");
        contact.setEmail("casuser@example.org");
        contact.setPhone("13477465421");
        registeredService.getContacts().add(contact);
        val listener = new RegisteredServicesEventListener(this.servicesManager, casProperties, communicationsManager);
        val event = new CasRegisteredServiceExpiredEvent(this, registeredService, false);
        listener.handleRegisteredServiceExpiredEvent(event);
    }

    @Test
    public void verifyServiceExpirationWithRemovalEvent() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val contact = new DefaultRegisteredServiceContact();
        contact.setName("Test");
        contact.setEmail("casuser@example.org");
        contact.setPhone("13477465421");
        registeredService.getContacts().add(contact);
        val listener = new RegisteredServicesEventListener(this.servicesManager, casProperties, communicationsManager);
        val event = new CasRegisteredServiceExpiredEvent(this, registeredService, true);
        listener.handleRegisteredServiceExpiredEvent(event);
    }

    @TestConfiguration
    public static class RegisteredServicesEventListenerTestConfiguration {

        @Bean
        public SmsSender smsSender() {
            return new MockSmsSender();
        }
    }
}
