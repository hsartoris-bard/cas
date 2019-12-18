package org.apereo.cas.gua.impl;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import com.google.common.io.ByteSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchResult;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * This is {@link LdapUserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LdapUserGraphicalAuthenticationRepository implements UserGraphicalAuthenticationRepository {
    private static final long serialVersionUID = 421732017215881244L;

    private final CasConfigurationProperties casProperties;


    @Override
    public ByteSource getGraphics(final String username) {
        try {
            val gua = casProperties.getAuthn().getGua();
            val response = searchForId(username);
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getResult().getEntry();
                val imageType = gua.getLdap().getImageType();
                switch (imageType) {
                    case "base64":
                        LOGGER.debug("Retrieving base64 image for user [{}]", username);
                        val base64attr = entry.getAttribute(gua.getLdap().getImageAttribute());
                        if (base64attr != null)
                            return ByteSource.wrap(base64attr.getBinaryValue());
                        break;
                    case "url":
                    case "uri":
                        LOGGER.debug("Retrieving image URI for user [{}]", username);
                        val stringAttr = LdapUtils.getString(entry, 
                                gua.getLdap().getImageAttribute(),
                                gua.getDefaultImageUri());
                        LOGGER.debug("Returning retrieved bytes");
                        val imageByteSource = getImageByUri(stringAttr);
                        if (imageByteSource == null)
                            LOGGER.debug("imageByteSource is null");
                        else if (imageByteSource.isEmpty())
                            LOGGER.debug("imageByteSource is empty");
                        else
                            return imageByteSource;
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ByteSource.empty();
    }

    private ByteSource getImageByUri(final String uri) throws IOException {
        LOGGER.debug("Attempting to retrieve image from [{}]", uri);
        val url = new URL(uri);
        val img = ImageIO.read(url);
        val ext = uri.substring(uri.lastIndexOf(".") + 1);
        LOGGER.debug("Retrieved image with extension [{}], height [{}], width [{}]", 
                ext, img.getHeight(), img.getWidth());
        val baos = new ByteArrayOutputStream();
        ImageIO.write(img, ext, baos);
        return ByteSource.wrap(baos.toByteArray());
    }

    private Response<SearchResult> searchForId(final String id) throws LdapException {
        val gua = casProperties.getAuthn().getGua();
        val filter = LdapUtils.newLdaptiveSearchFilter(gua.getLdap().getSearchFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
            CollectionUtils.wrap(id));
        return LdapUtils.executeSearchOperation(
            LdapUtils.newLdaptiveConnectionFactory(gua.getLdap()),
            gua.getLdap().getBaseDn(),
            filter,
            0,
            new String[]{gua.getLdap().getImageAttribute()},
            ReturnAttributes.ALL_USER.value());
    }

}
