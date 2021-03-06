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

/** Unit tests for {@link OAuth2TokenRevocationConfiguration}. */

package org.geant.idpextension.oidc.security.impl;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.geant.idpextension.oidc.criterion.ClientInformationCriterion;
import org.geant.idpextension.oidc.profile.spring.factory.BasicJWKCredentialFactoryBean;
import org.geant.idpextension.oidc.security.impl.OIDCClientInformationSignatureSigningParametersResolver.ParameterType;
import org.mockito.Mockito;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

/**
 * Tests for {@link OIDCClientInformationSignatureSigningParametersResolver}.
 */
public class OIDCClientInformationSignatureSigningParametersResolverTest {

    private OIDCClientInformationSignatureSigningParametersResolver resolver;

    private CriteriaSet criteria;

    private OIDCClientMetadata metaData;

    @BeforeMethod
    protected void setUp() throws Exception {
        resolver = new OIDCClientInformationSignatureSigningParametersResolver();
        // Signing configuration
        List<SignatureSigningConfiguration> configs = new ArrayList<SignatureSigningConfiguration>();
        SignatureSigningConfiguration signConfig = Mockito.mock(SignatureSigningConfiguration.class);
        Mockito.when(signConfig.getSignatureAlgorithms())
                .thenReturn(Arrays.asList("RS256", "HS256", "HS384", "HS512", "ES256", "ES384", "ES512"));
        List<Credential> signCreds = new ArrayList<Credential>();
        BasicJWKCredentialFactoryBean factory = new BasicJWKCredentialFactoryBean();
        factory.setJWKResource(new ClassPathResource("credentials/idp-signing-es.jwk"));
        factory.afterPropertiesSet();
        signCreds.add(factory.getObject());
        factory = new BasicJWKCredentialFactoryBean();
        factory.setJWKResource(new ClassPathResource("credentials/idp-signing-es384.jwk"));
        factory.afterPropertiesSet();
        signCreds.add(factory.getObject());
        factory = new BasicJWKCredentialFactoryBean();
        factory.setJWKResource(new ClassPathResource("credentials/idp-signing-es521.jwk"));
        factory.afterPropertiesSet();
        signCreds.add(factory.getObject());
        factory = new BasicJWKCredentialFactoryBean();
        factory.setJWKResource(new ClassPathResource("credentials/idp-signing-rs.jwk"));
        factory.afterPropertiesSet();
        signCreds.add(factory.getObject());
        Mockito.when(signConfig.getSigningCredentials()).thenReturn(signCreds);
        configs.add(signConfig);
        criteria = new CriteriaSet(new SignatureSigningConfigurationCriterion(configs));
        metaData = new OIDCClientMetadata();
        metaData.setIDTokenJWSAlg(JWSAlgorithm.RS256);
        metaData.setUserInfoJWSAlg(JWSAlgorithm.ES256);
        OIDCClientInformation clientInformation =
                new OIDCClientInformation(new ClientID(), new Date(), metaData, new Secret("abcdefgh"));
        criteria.add(new ClientInformationCriterion(clientInformation));
    }

    @Test
    public void testIdTokenParameters() throws ResolverException {
        SignatureSigningParameters params = resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "RS256");
        Assert.assertTrue(params.getSigningCredential().getPrivateKey() instanceof RSAPrivateKey);
    }

    @Test
    public void testDefaultIdTokenParameters() throws ResolverException {
        metaData.setIDTokenJWSAlg(null);
        SignatureSigningParameters params = resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "RS256");
        Assert.assertTrue(params.getSigningCredential().getPrivateKey() instanceof RSAPrivateKey);
    }

    @Test
    public void testIdTokenParametersHS() throws ResolverException {
        metaData.setIDTokenJWSAlg(JWSAlgorithm.HS256);
        SignatureSigningParameters params = resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "HS256");
        Assert.assertNotNull(params.getSigningCredential().getSecretKey());
    }

    @Test
    public void testUserInfoParameters() throws ResolverException {
        resolver.setParameterType(ParameterType.USERINFO_SIGNING);
        SignatureSigningParameters params = resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "ES256");
        Assert.assertTrue(params.getSigningCredential().getPrivateKey() instanceof ECPrivateKey);
        Assert.assertEquals(
                Curve.forECParameterSpec(
                        ((java.security.interfaces.ECKey) params.getSigningCredential().getPrivateKey()).getParams()),
                Curve.P_256);
    }

    @Test
    public void testUserInfoParametersES384() throws ResolverException {
        metaData.setUserInfoJWSAlg(JWSAlgorithm.ES384);
        resolver.setParameterType(ParameterType.USERINFO_SIGNING);
        SignatureSigningParameters params = resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "ES384");
        Assert.assertTrue(params.getSigningCredential().getPrivateKey() instanceof ECPrivateKey);
        Assert.assertEquals(
                Curve.forECParameterSpec(
                        ((java.security.interfaces.ECKey) params.getSigningCredential().getPrivateKey()).getParams()),
                Curve.P_384);
    }

    @Test
    public void testUserInfoParametersES512() throws ResolverException {
        metaData.setUserInfoJWSAlg(JWSAlgorithm.ES512);
        resolver.setParameterType(ParameterType.USERINFO_SIGNING);
        SignatureSigningParameters params = resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "ES512");
        Assert.assertTrue(params.getSigningCredential().getPrivateKey() instanceof ECPrivateKey);
        Assert.assertEquals(
                Curve.forECParameterSpec(
                        ((java.security.interfaces.ECKey) params.getSigningCredential().getPrivateKey()).getParams()),
                Curve.P_521);
    }

    protected void testSigningValidationHS256(ParameterType parameterType) throws ResolverException {
        resolver.setParameterType(parameterType);
        OIDCSignatureValidationParameters params = (OIDCSignatureValidationParameters) resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "HS256");
        Assert.assertTrue(params.getValidationCredentials().size() == 1);
        Assert.assertNotNull(params.getValidationCredentials().get(0).getSecretKey());
    }

    protected void testSigningValidationHS384(ParameterType parameterType) throws ResolverException {
        resolver.setParameterType(parameterType);
        OIDCSignatureValidationParameters params = (OIDCSignatureValidationParameters) resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "HS384");
        Assert.assertTrue(params.getValidationCredentials().size() == 1);
        Assert.assertNotNull(params.getValidationCredentials().get(0).getSecretKey());
    }

    protected void testSigningValidationHS512(ParameterType parameterType) throws ResolverException {
        resolver.setParameterType(parameterType);
        OIDCSignatureValidationParameters params = (OIDCSignatureValidationParameters) resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "HS512");
        Assert.assertTrue(params.getValidationCredentials().size() == 1);
        Assert.assertNotNull(params.getValidationCredentials().get(0).getSecretKey());
    }

    protected void testSigningValidationES256(ParameterType parameterType) throws ResolverException {
        resolver.setParameterType(parameterType);
        OIDCSignatureValidationParameters params = (OIDCSignatureValidationParameters) resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "ES256");
        Assert.assertTrue(params.getValidationCredentials().size() == 1);
        Assert.assertEquals(Curve.forECParameterSpec(
                ((java.security.interfaces.ECKey) params.getValidationCredentials().get(0).getPublicKey()).getParams()),
                Curve.P_256);
    }

    protected void testSigningValidationES384(ParameterType parameterType) throws ResolverException {
        resolver.setParameterType(parameterType);
        OIDCSignatureValidationParameters params = (OIDCSignatureValidationParameters) resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "ES384");
        Assert.assertTrue(params.getValidationCredentials().size() == 1);
        Assert.assertEquals(Curve.forECParameterSpec(
                ((java.security.interfaces.ECKey) params.getValidationCredentials().get(0).getPublicKey()).getParams()),
                Curve.P_384);
    }

    protected void testSigningValidationES512(ParameterType parameterType) throws ResolverException {
        resolver.setParameterType(parameterType);
        OIDCSignatureValidationParameters params = (OIDCSignatureValidationParameters) resolver.resolveSingle(criteria);
        Assert.assertEquals(params.getSignatureAlgorithm(), "ES512");
        Assert.assertTrue(params.getValidationCredentials().size() == 2);
        Assert.assertEquals(Curve.forECParameterSpec(
                ((java.security.interfaces.ECKey) params.getValidationCredentials().get(0).getPublicKey()).getParams()),
                Curve.P_521);
        Assert.assertEquals(Curve.forECParameterSpec(
                ((java.security.interfaces.ECKey) params.getValidationCredentials().get(1).getPublicKey()).getParams()),
                Curve.P_521);
    }
}