package org.apereo.cas.util.io;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CommunicationsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
},
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000"
    })
@Tag("Mail")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 25000)
public class CommunicationsManagerTests {
    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Test
    public void verifyMailSender() {
        assertTrue(communicationsManager.isMailSenderDefined());

        var props = new EmailProperties();
        props.setText("Test Body");
        props.setSubject("Subject");
        props.setFrom("cas@example.org");
        props.setCc("cc@example.org");
        props.setBcc("bcc@example.org");

        assertTrue(communicationsManager.email(props, "sample@example.org", props.getFormattedBody()));
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("casuser");
        when(p.getAttributes()).thenReturn(CollectionUtils.wrap("email", List.of("cas@example.org")));
        assertTrue(communicationsManager.email(p, "email", props, props.getFormattedBody()));
    }

    @Test
    public void verifyMailSenderWithTemplateBody() throws Exception {
        assertTrue(communicationsManager.isMailSenderDefined());

        val tempFile = Files.createTempFile("prefix", "postfix").toFile();
        FileUtils.write(tempFile, "This is an example with %s %s", StandardCharsets.UTF_8);

        val props = new EmailProperties();
        props.setText(tempFile.getCanonicalPath());
        props.setSubject("Subject");
        props.setFrom("cas@example.org");

        val body = props.getFormattedBody("param1", "param2");
        assertTrue(communicationsManager.email(props, "sample@example.org", body));
    }
}
