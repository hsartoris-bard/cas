package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
public class OidcJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    private static File KEYSTORE;

    @BeforeAll
    public static void setup() {
        KEYSTORE = new File(FileUtils.getTempDirectoryPath(), "something.jwks");
        if (KEYSTORE.exists()) {
            assertTrue(KEYSTORE.delete());
        }
    }

    @Test
    public void verifyOperation() {
        oidcJsonWebKeystoreGeneratorService.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }

    @Test
    public void verifyCurve256() {
        val properties = new OidcProperties();
        properties.setJwksType("ec");
        properties.setJwksKeySize(256);
        val service = new OidcJsonWebKeystoreGeneratorService(properties);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }

    @Test
    public void verifyCurve384() {
        val properties = new OidcProperties();
        properties.setJwksType("ec");
        properties.setJwksKeySize(384);
        val service = new OidcJsonWebKeystoreGeneratorService(properties);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }

    @Test
    public void verifyCurve521() {
        val properties = new OidcProperties();
        properties.setJwksType("ec");
        properties.setJwksKeySize(521);
        val service = new OidcJsonWebKeystoreGeneratorService(properties);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }
}
