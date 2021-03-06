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

package org.geant.idpextension.oidc.attribute.filter.spring.impl;

import org.geant.idpextension.oidc.attribute.filter.spring.matcher.impl.AttributeInOIDCRequestedClaimsRuleParser;
import org.geant.idpextension.oidc.attribute.filter.spring.policyrule.impl.AttributeOIDCScopeRuleParser;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;

/** Namespace handler for the oidc specific attribute filter engine functions. */
public class AttributeFilterNamespaceHandler extends BaseSpringNamespaceHandler {

    /** oidc namespace. */
    public static final String NAMESPACE = "org.geant.idpextension.oidc.attribute.filter";

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Policy rules
        registerBeanDefinitionParser(AttributeOIDCScopeRuleParser.SCHEMA_TYPE_AFP, new AttributeOIDCScopeRuleParser());
        // Matchers
        registerBeanDefinitionParser(AttributeInOIDCRequestedClaimsRuleParser.SCHEMA_TYPE_AFP,
                new AttributeInOIDCRequestedClaimsRuleParser());
    }
}