package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link EduPersonTargetedIdAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EduPersonTargetedIdAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {
    /**
     * The attribute name generated by this policy.
     */
    public static final String ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID = "eduPersonTargetedID";

    /**
     * The attribute urn generated by this policy.
     */
    public static final String ATTRIBUTE_URN_EDU_PERSON_TARGETED_ID = "urn:oid:1.3.6.1.4.1.5923.1.1.1.10";

    private static final long serialVersionUID = -1283755507124862357L;

    @JsonProperty
    @ExpressionLanguageCapable
    private String salt;

    @JsonProperty
    @ExpressionLanguageCapable
    private String attribute;

    @JsonProperty
    private boolean useUniformResourceName;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final ApplicationContext applicationContext,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceServiceProviderMetadataFacade facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {
        val releaseAttributes = new HashMap<String, List<Object>>();

        val resolvedSalt = SpringExpressionLanguageValueResolver.getInstance().resolve(this.salt);
        val persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator(resolvedSalt);

        val resolvedAttrs = SpringExpressionLanguageValueResolver.getInstance().resolve(this.attribute);
        persistentIdGenerator.setAttribute(resolvedAttrs);

        val principalId = persistentIdGenerator.determinePrincipalIdFromAttributes(context.getPrincipal().getId(), attributes);
        LOGGER.debug("Selected principal id [{}] to generate [{}] for service [{}]",
            principalId, ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID, context.getService());
        val result = persistentIdGenerator.generate(principalId, context.getService());
        val attrName = this.useUniformResourceName ? ATTRIBUTE_URN_EDU_PERSON_TARGETED_ID : ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID;
        releaseAttributes.put(attrName, CollectionUtils.wrapList(result));
        LOGGER.debug("Calculated [{}] attribute as [{}]", attrName, result);
        return releaseAttributes;
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(
        final RegisteredServiceAttributeReleasePolicyContext context) {
        return this.useUniformResourceName
            ? CollectionUtils.wrapList(ATTRIBUTE_URN_EDU_PERSON_TARGETED_ID)
            : CollectionUtils.wrapList(ATTRIBUTE_NAME_EDU_PERSON_TARGETED_ID);
    }
}
