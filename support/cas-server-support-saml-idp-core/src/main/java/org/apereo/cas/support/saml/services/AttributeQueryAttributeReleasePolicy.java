package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * This is {@link AttributeQueryAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class AttributeQueryAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -2283755507124862357L;

    public AttributeQueryAttributeReleasePolicy() {
        setAuthorizedToReleaseAuthenticationAttributes(false);
    }

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final ApplicationContext applicationContext,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceServiceProviderMetadataFacade facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {

        LOGGER.trace("Evaluating attribute release policy for service request [{}]", context.getService());
        return authorizeReleaseOfAllowedAttributes(context, attributes);
    }

    @Override
    protected boolean supports(final RegisteredServiceAttributeReleasePolicyContext context) {
        val serviceAttributes = context.getService().getAttributes().getOrDefault("owner", List.of());
        return serviceAttributes.contains(SamlAttributeQueryTicket.class.getName());
    }
}
