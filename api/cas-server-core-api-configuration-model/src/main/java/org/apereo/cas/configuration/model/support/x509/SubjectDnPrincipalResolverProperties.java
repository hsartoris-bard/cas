package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import javax.security.auth.x500.X500Principal;
import java.io.Serializable;

/**
 * This is {@link SubjectDnPrincipalResolverProperties}, properties for SUBJECT_DN principal resolver type.
 * Allows choosing which method for retrieving subject DN is called.
 * @author Hal Deadman
 * @since 6.2
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
public class SubjectDnPrincipalResolverProperties implements Serializable {

    private static final long serialVersionUID = -1833042842488884318L;

    /**
     * Format of subject DN to use.
     */
    private SubjectDnPrincipalResolverProperties.SubjectDnFormat format = SubjectDnPrincipalResolverProperties.SubjectDnFormat.DEFAULT;

    /**
     * The format of subject dn to be used.
     */
    public enum SubjectDnFormat {
        /**
         * Denigrated result of calling certificate.getSubjectDN() method.
         * Javadocs designate this method as "denigrated" for not being portable and/or not being well defined.
         * It is what has been used by CAS for a long time so it remains the default.
         * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/X509Certificate.html#getIssuerDN()">DENIGRATED</a>
         */
        DEFAULT,
        /**
         * RFC 1779 String format of Distinguished Names.
         * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/x500/X500Principal.html#getName()">RFC1779</a>
         */
        RFC1779,
        /**
         * RFC 2253 String format of Distinguished Names.
         * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/x500/X500Principal.html#getName()">RFC2253</a>
         */
        RFC2253,
        /**
         * Canonical String format of Distinguished Names.
         * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/x500/X500Principal.html#getName()">CANONICAL</a>
         */
        CANONICAL
    }

    public String getSubjectDnFormat() {
        switch (format) {
            case RFC1779:
                return X500Principal.RFC1779;
            case RFC2253:
                return X500Principal.RFC2253;
            case CANONICAL:
                return X500Principal.CANONICAL;
            default:
                return null;
        }
    }
}
