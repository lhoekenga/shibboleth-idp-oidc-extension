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

package org.geant.idpextension.oidc.algorithm.descriptors;

import javax.annotation.Nonnull;

import org.geant.idpextension.oidc.crypto.support.KeyManagementConstants;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.algorithm.SymmetricKeyWrapAlgorithm;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Algorithm descriptor for JWE key management algorithm: AES128GCMKW.
 * 
 */
public class SymmetricKeyWrapA128GCMKW implements SymmetricKeyWrapAlgorithm {

    /** {@inheritDoc} */
    @Nonnull
    @NotEmpty
    public String getKey() {
        return JCAConstants.KEY_ALGO_AES;
    }

    /** {@inheritDoc} */
    @Nonnull
    @NotEmpty
    public String getURI() {
        return KeyManagementConstants.ALGO_ID_ALG_AES_128_GCM_KW;
    }

    /** {@inheritDoc} */
    @Nonnull
    public AlgorithmType getType() {
        return AlgorithmType.SymmetricKeyWrap;
    }

    /** {@inheritDoc} */
    @Nonnull
    public String getJCAAlgorithmID() {
        return JCAConstants.KEYWRAP_ALGO_AES;
    }

    @Override
    public Integer getKeyLength() {
        return 128;
    }

}
