package org.apereo.cas.adaptors.x509.authentication;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handles the fetching of CRL objects based on resources.
 * Supports http/ldap resources.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class ResourceCRLFetcher implements CRLFetcher {
    @Override
    public Collection<X509CRL> fetch(final Collection<Resource> crls) {
        return crls.stream()
            .map(Unchecked.function(crl -> {
                LOGGER.debug("Fetching CRL data from [{}]", crl);
                try (val ins = crl.getInputStream()) {
                    return (X509CRL) CertUtils.getCertificateFactory().generateCRL(ins);
                }
            }))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException          the exception thrown if resources cant be fetched
     * @throws CRLException         the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final String crl) throws IOException, CRLException, CertificateException {
        return fetch(new URL(crl));
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException          the exception thrown if resources cant be fetched
     * @throws CRLException         the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final URI crl) throws IOException, CRLException, CertificateException {
        return fetch(crl.toURL());
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException          the exception thrown if resources cant be fetched
     * @throws CRLException         the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final URL crl) throws IOException, CRLException, CertificateException {
        return fetch(new UrlResource(crl));
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide how the resource should be retrieved.
     *
     * @param crl the resource
     * @return the x 509 cRL
     * @throws IOException          the exception thrown if resources cant be fetched
     * @throws CRLException         the exception thrown if resources cant be fetched
     * @throws CertificateException the exception thrown if resources cant be fetched
     */
    @Override
    public X509CRL fetch(final Resource crl) throws IOException, CRLException, CertificateException {
        val results = fetch(CollectionUtils.wrap(crl));
        if (!results.isEmpty()) {
            return results.iterator().next();
        }
        LOGGER.warn("Unable to fetch [{}]", crl);
        return null;
    }
}
