/*
 * GÉANT BSD Software License
 *
 * Copyright (c) 2017 - 2020, GÉANT
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the GÉANT nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.geant.idpextension.oidc.metadata.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.geant.idpextension.oidc.metadata.resolver.MetadataValueResolver;
import org.geant.idpextension.oidc.metadata.resolver.RefreshableMetadataValueResolver;
import org.joda.time.DateTime;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import net.minidev.json.JSONObject;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

/**
 * An extension to {@link FilesystemProviderMetadataResolver} that enables some of the claims to be dynamically updated
 * outside the file.
 */
public class DynamicFilesystemProviderMetadataResolver extends FilesystemProviderMetadataResolver {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DynamicFilesystemProviderMetadataResolver.class);

    /** The map of dynamic metadata value resolvers, key corresponding to the name of the metadata field. */
    private Map<String, ? extends MetadataValueResolver> dynamicResolvers = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param metadata the metadata file
     * 
     * @throws IOException If the metedata cannot be loaded.
     */
    public DynamicFilesystemProviderMetadataResolver(@Nonnull final Resource metadata) throws IOException {
        super(metadata);
    }

    /**
     * Constructor.
     * 
     * @param backgroundTaskTimer timer used to refresh metadata in the background
     * @param metadata the metadata file
     * 
     * @throws IOException If the metedata cannot be loaded.
     */
    public DynamicFilesystemProviderMetadataResolver(@Nullable final Timer backgroundTaskTimer,
            @Nonnull final Resource metadata) throws IOException {
        super(backgroundTaskTimer, metadata);
    }

    /**
     * Set dynamic metadata value resolvers.
     * 
     * @param map What to set.
     */
    public void setDynamicValueResolvers(final Map<String, ? extends MetadataValueResolver> map) {
        dynamicResolvers = Constraint.isNotNull(map, "The map of dynamic metadata resolvers cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<OIDCProviderMetadata> resolve(ProfileRequestContext profileRequestContext)
            throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        final List<OIDCProviderMetadata> result = new ArrayList<OIDCProviderMetadata>();
        final Iterator<OIDCProviderMetadata> entities = super.resolve(profileRequestContext).iterator();
        while (entities.hasNext()) {
            final OIDCProviderMetadata entity = entities.next();
            final JSONObject entityJson = entity.toJSONObject();
            for (final String key : dynamicResolvers.keySet()) {
                log.debug("Starting to resolve value for {}", key);
                final MetadataValueResolver resolver = dynamicResolvers.get(key);
                try {
                    if (resolver instanceof RefreshableMetadataValueResolver) {
                        ((RefreshableMetadataValueResolver) resolver).refresh();
                    }
                    final Object value = resolver.resolveSingle(profileRequestContext);
                    if (value != null) {
                        entityJson.put(key, value);
                        log.debug("The field {} updated to the result", key);
                    }
                } catch (ResolverException e) {
                    log.warn("Could not resolve a value for {̛}, ignoring it.", key, e);
                }
            }
            try {
                result.add(OIDCProviderMetadata.parse(entityJson));
            } catch (ParseException e) {
                log.warn("The resulting provider metadata is not valid, ignoring it", e);
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected DateTime getMetadataUpdateTime() {
        DateTime updateTime = super.getMetadataUpdateTime();
        for (final String id : dynamicResolvers.keySet()) {
            final MetadataValueResolver resolver = dynamicResolvers.get(id);
            if (resolver instanceof RefreshableMetadataValueResolver) {
                if (((RefreshableMetadataValueResolver) resolver).getLastUpdate() == null) {
                    return DateTime.now();
                }
                if (((RefreshableMetadataValueResolver) resolver).getLastUpdate().isAfter(updateTime)) {
                    return ((RefreshableMetadataValueResolver) resolver).getLastUpdate();
                }
            }
        }
        return updateTime;
    }
}
