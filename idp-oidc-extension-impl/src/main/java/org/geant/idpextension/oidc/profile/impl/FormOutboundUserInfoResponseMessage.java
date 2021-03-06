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

import javax.annotation.Nonnull;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;

/**
 * Action that forms outbound message based on response context. Formed message is set to
 * {@link ProfileRequestContext#getOutboundMessageContext()}.
 * 
 * Actions assumes {@link OidcResponseContext#getProcessedToken()} returns signed, signed and encrypted or encrypted
 * response content if such is meant to be sent to the client. Otherwise actions assumes response content is located by
 * {@link OidcResponseContext#getUserInfo()}
 */
public class FormOutboundUserInfoResponseMessage extends AbstractOIDCTokenResponseAction {

    /** Class logger. */
    @Nonnull
    private Logger log = LoggerFactory.getLogger(FormOutboundUserInfoResponseMessage.class);

    /** {@inheritDoc} */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        UserInfoSuccessResponse resp;
        if (getOidcResponseContext().getProcessedToken() != null) {
            resp = new UserInfoSuccessResponse(getOidcResponseContext().getProcessedToken());
        } else if (getOidcResponseContext().getUserInfo() != null) {
            resp = new UserInfoSuccessResponse(getOidcResponseContext().getUserInfo());
        } else {
            log.error("{} no content to form userinfo response", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }
        ((MessageContext) getOidcResponseContext().getParent()).setMessage(resp);

    }
}