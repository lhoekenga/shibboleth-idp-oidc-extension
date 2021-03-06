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

package org.geant.idpextension.oidc.token.support;

import org.testng.annotations.Test;
import net.shibboleth.utilities.java.support.security.DataSealerException;
import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;

import java.text.ParseException;
import org.testng.Assert;

/**
 * Tests for {@link RefreshTokenClaimsSetTest}
 */
public class RefreshTokenClaimsSetTest extends BaseTokenClaimsSetTest {

    private RefreshTokenClaimsSet rfClaimsSet;

    protected void init() {
        AuthorizeCodeClaimsSet acClaimsSet =
                new AuthorizeCodeClaimsSet.Builder(new SecureRandomIdentifierGenerationStrategy(), clientID, issuer,
                        userPrincipal, subject, iat, exp, authTime, redirectURI, scope).setACR(acr).build();
        rfClaimsSet = new RefreshTokenClaimsSet(acClaimsSet, iat, exp);
    }

    @Test
    public void testSerialization() throws ParseException, DataSealerException {
        init();
        RefreshTokenClaimsSet rfClaimsSet2 = RefreshTokenClaimsSet.parse(rfClaimsSet.serialize());
        Assert.assertEquals(rfClaimsSet2.getACR(), acr.getValue());
        RefreshTokenClaimsSet rfClaimsSet3 = RefreshTokenClaimsSet.parse(rfClaimsSet2.serialize(sealer), sealer);
        Assert.assertEquals(rfClaimsSet3.getACR(), acr.getValue());
    }

    @Test(expectedExceptions = ParseException.class)
    public void testSerializationWrongType() throws ParseException {
        AuthorizeCodeClaimsSet accessnClaimsSet =
                new AuthorizeCodeClaimsSet.Builder(new SecureRandomIdentifierGenerationStrategy(), clientID, issuer,
                        userPrincipal, subject, iat, exp, authTime, redirectURI, scope).build();
        rfClaimsSet = RefreshTokenClaimsSet.parse(accessnClaimsSet.serialize());
    }

}