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

package org.geant.idpextension.oidc.profile.impl;

import java.net.URI;

import org.geant.idpextension.oidc.profile.context.navigate.DefaultRequestResponseModeLookupFunction;
import org.geant.idpextension.oidc.profile.context.navigate.DefaultRequestStateLookupFunction;
import org.geant.idpextension.oidc.profile.context.navigate.ValidatedRedirectURILookupFunction;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;

/**
 * This action reads an event from the configured {@link EventContext} lookup strategy, constructs an OIDC
 * authentication error response message and attaches it as the outbound message.
 */
public class BuildAuthenticationErrorResponseFromEvent
        extends AbstractBuildErrorResponseFromEvent<AuthenticationErrorResponse> {

    @SuppressWarnings("rawtypes")
    @Override
    protected AuthenticationErrorResponse buildErrorResponse(ErrorObject error,
            ProfileRequestContext profileRequestContext) {
        URI redirectURI = new ValidatedRedirectURILookupFunction().apply(profileRequestContext);
        if (redirectURI == null) {
            // No validated redirect uri to return reply to.
            return null;
        }
        return new AuthenticationErrorResponse(redirectURI, error,
                new DefaultRequestStateLookupFunction().apply(profileRequestContext),
                new DefaultRequestResponseModeLookupFunction().apply(profileRequestContext));
    }

}
