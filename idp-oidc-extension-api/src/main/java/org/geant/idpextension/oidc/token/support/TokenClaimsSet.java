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

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.ClaimsRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.ACR;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.shibboleth.utilities.java.support.security.DataSealer;
import net.shibboleth.utilities.java.support.security.DataSealerException;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;

import java.net.URI;
import java.text.ParseException;

/**
 * Class to extend for token claims sets. Offers the base functionality to Authorize Code, Refresh Token and Access
 * Token.
 */
public class TokenClaimsSet {

    /** Identifier for the token. */
    public static final String KEY_AC_ID = "jti";

    /** Type of the token. */
    public static final String KEY_TYPE = "type";

    /** OP issuer. */
    public static final String KEY_ISSUER = "iss";

    /** User principal representing authenticated user. */
    public static final String KEY_USER_PRINCIPAL = "prncpl";

    /** Subject of the user. */
    public static final String KEY_SUBJECT = "sub";

    /** Client id of the rp the token is generated for. */
    public static final String KEY_CLIENTID = "clid";

    /** Expiration time of the token. */
    public static final String KEY_EXPIRATION_TIME = "exp";

    /** Issue time of the token. */
    public static final String KEY_ISSUED_AT = "iat";

    /** Authentication context class reference value of the performed authentication. */
    public static final String KEY_ACR = "acr";

    /** Nonce of the original authentication request. */
    public static final String KEY_NONCE = "nonce";

    /** Authentication time of the performed authentication. */
    public static final String KEY_AUTH_TIME = "auth_time";

    /** Redirect uri of the original authentication request. */
    public static final String KEY_REDIRECT_URI = "redirect_uri";

    /** Scope of the original authentication request. */
    public static final String KEY_SCOPE = "scope";

    /** Claims request of the original authentication request. */
    public static final String KEY_CLAIMS = "claims";

    /** Claims set for token delivery. */
    public static final String KEY_DELIVERY_CLAIMS = "dl_claims";

    /** Claims set for token delivery, id token only. */
    public static final String KEY_DELIVERY_CLAIMS_IDTOKEN = "dl_claims_id";

    /** Claims set for token delivery, user info only. */
    public static final String KEY_DELIVERY_CLAIMS_USERINFO = "dl_claims_ui";

    /** Claims/Attributes requiring consent. */
    public static final String KEY_CONSENTABLE_CLAIMS = "cnsntbl_claims";

    /** Claims/Attributes having consent. */
    public static final String KEY_CONSENTED_CLAIMS = "cnsntd_claims";

    /** Claims set for the claim. */
    protected JWTClaimsSet tokenClaimsSet;

    /** Class logger. */
    @Nonnull
    private Logger log = LoggerFactory.getLogger(TokenClaimsSet.class);

    /**
     * Constructor.
     */
    protected TokenClaimsSet() {

    }

    /**
     * Constructor for token claims set.
     * 
     * @param tokenType Token type. Must not be NULL.
     * @param tokenID identifier for the token. Must not be NULL.
     * @param clientID Client Id of the rp. Must not be NULL.
     * @param issuer OP issuer value. Must not be NULL.
     * @param userPrincipal User Principal of the authenticated user. Must not be NULL.
     * @param subject subject of the authenticated user. Must not be NULL.
     * @param acr Authentication context class reference value of the authentication. May be NULL.
     * @param iat Issue time of the token. Must not be NULL.
     * @param exp Expiration time of the token. Must not be NULL.
     * @param nonce Nonce of the authentication request. May be NULL.
     * @param authTime Authentication time of the user. Must not be NULL.
     * @param redirectURI Validated redirect URI of the authentication request. Must not be NULL.
     * @param scope Scope of the authentication request. Must not be NULL.
     * @param claims Claims request of the authentication request. May be NULL.
     * @param dlClaims token delivery claims delivered both for id token and userinfo response. May be NULL.
     * @param dlClaimsID token delivery claims delivered for id token. May be NULL.
     * @param dlClaimsUI token delivery claims delivered for userinfo response. May be NULL.
     * @param consentableClaims consentable claims. May be NULL.
     * @param consentedClaims consented claims. May be NULL.
     * @throws RuntimeException if called with not allowed null parameters
     */
    // Checkstyle: CyclomaticComplexity OFF
    protected TokenClaimsSet(@Nonnull String tokenType, @Nonnull String tokenID, @Nonnull ClientID clientID,
            @Nonnull String issuer, @Nonnull String userPrincipal, @Nonnull String subject, @Nullable ACR acr,
            @Nonnull Date iat, @Nonnull Date exp, @Nullable Nonce nonce, @Nonnull Date authTime,
            @Nonnull URI redirectURI, @Nonnull Scope scope, @Nullable ClaimsRequest claims,
            @Nullable ClaimsSet dlClaims, @Nullable ClaimsSet dlClaimsID, @Nullable ClaimsSet dlClaimsUI,
            @Nullable JSONArray consentableClaims, @Nullable JSONArray consentedClaims) {
        if (tokenType == null || tokenID == null || clientID == null || issuer == null || userPrincipal == null
                || iat == null || exp == null || authTime == null || redirectURI == null || scope == null
                || subject == null) {
            throw new RuntimeException("Invalid parameters, programming error");
        }
        tokenClaimsSet = new JWTClaimsSet.Builder().claim(KEY_TYPE, tokenType).jwtID(tokenID)
                .claim(KEY_CLIENTID, clientID.getValue()).issuer(issuer).subject(subject)
                .claim(KEY_USER_PRINCIPAL, userPrincipal).claim(KEY_ACR, acr == null ? null : acr.getValue())
                .issueTime(iat).expirationTime(exp).claim(KEY_NONCE, nonce == null ? null : nonce.getValue())
                .claim(KEY_AUTH_TIME, authTime).claim(KEY_REDIRECT_URI, redirectURI.toString())
                .claim(KEY_SCOPE, scope.toString()).claim(KEY_CLAIMS, claims == null ? null : claims.toJSONObject())
                .claim(KEY_DELIVERY_CLAIMS, dlClaims == null ? null : dlClaims.toJSONObject())
                .claim(KEY_DELIVERY_CLAIMS_IDTOKEN, dlClaimsID == null ? null : dlClaimsID.toJSONObject())
                .claim(KEY_DELIVERY_CLAIMS_USERINFO, dlClaimsUI == null ? null : dlClaimsUI.toJSONObject())
                .claim(KEY_CONSENTABLE_CLAIMS, consentableClaims).claim(KEY_CONSENTED_CLAIMS, consentedClaims).build();

    }

    // Checkstyle: CyclomaticComplexity ON

    /**
     * Helper to verify parsed claims are what is expected.
     * 
     * @param tokenType The type of the expected token. Must not be NULL.
     * @param tokenClaimsSet token claims set Must not be NULL.
     * @throws ParseException if claims set is not expected one.
     */
    // Checkstyle: CyclomaticComplexity OFF
    protected static void verifyParsedClaims(@Nonnull String tokenType, @Nonnull JWTClaimsSet tokenClaimsSet)
            throws ParseException {
        // Check existence and type of mandatory fields and values
        if (!tokenType.equals(tokenClaimsSet.getClaims().get(KEY_TYPE))) {
            throw new ParseException("claim type value not matching", 0);
        }
        // Mandatory fields
        if (tokenClaimsSet.getStringClaim(KEY_ISSUER) == null) {
            throw new ParseException("claim iss must exist and not be null", 0);
        }
        if (tokenClaimsSet.getStringClaim(KEY_USER_PRINCIPAL) == null) {
            throw new ParseException("claim prncpl must exist and not be null", 0);
        }
        if (tokenClaimsSet.getStringClaim(KEY_SUBJECT) == null) {
            throw new ParseException("claim sub must exist and not be null", 0);
        }
        if (tokenClaimsSet.getStringClaim(KEY_CLIENTID) == null) {
            throw new ParseException("claim clid must exist and not be null", 0);
        }
        if (tokenClaimsSet.getDateClaim(KEY_EXPIRATION_TIME) == null) {
            throw new ParseException("claim exp must exist and not be null", 0);
        }
        if (tokenClaimsSet.getDateClaim(KEY_ISSUED_AT) == null) {
            throw new ParseException("claim iat must exist and not be null", 0);
        }
        if (tokenClaimsSet.getStringClaim(KEY_AC_ID) == null) {
            throw new ParseException("claim jti must exist and not be null", 0);
        }
        if (tokenClaimsSet.getDateClaim(KEY_AUTH_TIME) == null) {
            throw new ParseException("claim auth_time must exist and not be null", 0);
        }
        if (tokenClaimsSet.getStringClaim(KEY_REDIRECT_URI) == null) {
            throw new ParseException("claim redirect_uri must exist and not be null", 0);
        }
        if (tokenClaimsSet.getStringClaim(KEY_SCOPE) == null) {
            throw new ParseException("claim scope must exist and not be null", 0);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_ACR)) {
            tokenClaimsSet.getStringClaim(KEY_ACR);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_CONSENTABLE_CLAIMS)
                && !(tokenClaimsSet.getClaim(KEY_CONSENTABLE_CLAIMS) instanceof JSONArray)) {
            throw new ParseException("consentable claims is of wrong type", 0);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_CONSENTED_CLAIMS)
                && !(tokenClaimsSet.getClaim(KEY_CONSENTED_CLAIMS) instanceof JSONArray)) {
            throw new ParseException("consented claims is of wrong type", 0);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_CLAIMS)) {
            tokenClaimsSet.getJSONObjectClaim(KEY_CLAIMS);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_DELIVERY_CLAIMS)) {
            tokenClaimsSet.getJSONObjectClaim(KEY_DELIVERY_CLAIMS);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_DELIVERY_CLAIMS_IDTOKEN)) {
            tokenClaimsSet.getJSONObjectClaim(KEY_DELIVERY_CLAIMS_IDTOKEN);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_DELIVERY_CLAIMS_USERINFO)) {
            tokenClaimsSet.getJSONObjectClaim(KEY_DELIVERY_CLAIMS_USERINFO);
        }
        if (tokenClaimsSet.getClaims().containsKey(KEY_NONCE)) {
            tokenClaimsSet.getStringClaim(KEY_NONCE);
        }

    }
    // Checkstyle: CyclomaticComplexity ON

    /**
     * Serialize the token as JSON String.
     * 
     * @return token as JSON String
     */
    public String serialize() {
        return tokenClaimsSet.toJSONObject().toJSONString();
    }

    /**
     * Serialize the token as JSON String wrapped with sealer.
     * 
     * @param dataSealer data sealer to wrap the JSON serialization
     * @return token as JSON String wrapped with sealer
     * @throws DataSealerException is thrown if unwrapping fails
     */
    public String serialize(@Nonnull DataSealer dataSealer) throws DataSealerException {
        String wrapped = dataSealer.wrap(serialize(), tokenClaimsSet.getExpirationTime().getTime());
        return wrapped;
    }

    /**
     * Get the token claims set.
     * 
     * @return token claims set
     */
    @Nonnull
    public JWTClaimsSet getClaimsSet() {
        return tokenClaimsSet;
    }

    /**
     * Check if the token is expired.
     * 
     * @return true if the token is expired, otherwise false.
     */
    public boolean isExpired() {
        return tokenClaimsSet.getExpirationTime().before(new Date());
    }

    /**
     * Get expiration time of the token.
     * 
     * @return expiration time of the token.
     */
    @Nonnull
    public Date getExp() {
        return tokenClaimsSet.getExpirationTime();
    }

    /**
     * Get redirect uri of the request.
     * 
     * @return redirect uri of the request, null if not located.
     */
    @Nonnull
    public URI getRedirectURI() {
        try {
            return URI.create(tokenClaimsSet.getStringClaim(KEY_REDIRECT_URI));
        } catch (ParseException e) {
            log.error("error parsing redirect uri from token", e.getMessage());
        }
        // should never happen, programming error.
        return null;
    }

    /**
     * Get acr of the performed authentication.
     * 
     * @return acr of the performed authentication.
     */
    @Nonnull
    public String getACR() {
        return (String) tokenClaimsSet.getClaim(KEY_ACR);
    }

    /**
     * Get principal of the user.
     * 
     * @return principal of the user.
     */
    @Nonnull
    public String getPrincipal() {
        return (String) tokenClaimsSet.getClaim(KEY_USER_PRINCIPAL);
    }

    /**
     * Get auth time of the user.
     * 
     * @return auth time of the user.
     */
    @Nonnull
    public Date getAuthenticationTime() {
        try {
            return tokenClaimsSet.getDateClaim(KEY_AUTH_TIME);
        } catch (ParseException e) {
            log.error("Error parsing auth time {}", tokenClaimsSet.getClaim(KEY_AUTH_TIME));
            // should never happen, programming error.
            return null;
        }
    }

    /**
     * Get nonce of the authentication request.
     * 
     * @return nonce of the authentication request.
     */
    @Nonnull
    public Nonce getNonce() {
        if (tokenClaimsSet.getClaim(KEY_NONCE) == null) {
            return null;
        }
        return new Nonce((String) tokenClaimsSet.getClaim(KEY_NONCE));
    }

    /**
     * Get claims request of the authentication request.
     * 
     * @return claims request in authentication request, null if not existing.
     */
    @Nullable
    public ClaimsRequest getClaimsRequest() {
        if (tokenClaimsSet.getClaim(KEY_CLAIMS) == null) {
            return null;
        }
        try {
            return ClaimsRequest.parse(tokenClaimsSet.getJSONObjectClaim(KEY_CLAIMS));
        } catch (ParseException e) {
            log.error("Error parsing claims request {}", tokenClaimsSet.getClaim(KEY_CLAIMS));
            return null;
        }
    }

    /**
     * Get token delivery claims.
     * 
     * @return token delivery claims
     */
    public ClaimsSet getDeliveryClaims() {
        TokenDeliveryClaimsClaimsSet claimsSet = new TokenDeliveryClaimsClaimsSet();
        try {
            JSONObject claims = tokenClaimsSet.getJSONObjectClaim(KEY_DELIVERY_CLAIMS);
            if (claims == null) {
                return null;
            }
            claimsSet.putAll(claims);
        } catch (ParseException e) {
            log.error("Error parsing delivery claims {}", tokenClaimsSet.getClaim(KEY_DELIVERY_CLAIMS));
            return null;
        }
        return claimsSet;
    }

    /**
     * Get id token token delivery claims.
     * 
     * @return id token token delivery claims
     */
    public ClaimsSet getIDTokenDeliveryClaims() {
        TokenDeliveryClaimsClaimsSet claimsSet = new TokenDeliveryClaimsClaimsSet();
        try {
            JSONObject claims = tokenClaimsSet.getJSONObjectClaim(KEY_DELIVERY_CLAIMS_IDTOKEN);
            if (claims == null) {
                return null;
            }
            claimsSet.putAll(claims);
        } catch (ParseException e) {
            log.error("Error parsing id token delivery claims {}",
                    tokenClaimsSet.getClaim(KEY_DELIVERY_CLAIMS_IDTOKEN));
            return null;
        }
        return claimsSet;
    }

    /**
     * Get user info response token delivery claims.
     * 
     * @return user info response token delivery claims
     */
    public ClaimsSet getUserinfoDeliveryClaims() {
        TokenDeliveryClaimsClaimsSet claimsSet = new TokenDeliveryClaimsClaimsSet();
        try {
            JSONObject claims = tokenClaimsSet.getJSONObjectClaim(KEY_DELIVERY_CLAIMS_USERINFO);
            if (claims == null) {
                return null;
            }
            claimsSet.putAll(claims);
        } catch (ParseException e) {
            log.error("Error parsing id token delivery claims {}",
                    tokenClaimsSet.getClaim(KEY_DELIVERY_CLAIMS_USERINFO));
            return null;
        }
        return claimsSet;
    }

    /**
     * Get consentable claims.
     * 
     * @return consentable claims
     */
    public JSONArray getConsentableClaims() {
        return (JSONArray) tokenClaimsSet.getClaim(KEY_CONSENTABLE_CLAIMS);
    }

    /**
     * Get consented claims.
     * 
     * @return consented claims
     */
    public JSONArray getConsentedClaims() {
        return (JSONArray) tokenClaimsSet.getClaim(KEY_CONSENTED_CLAIMS);
    }

    /**
     * Get scope of the authentication request.
     * 
     * @return scope of the authentication request.
     */
    @Nonnull
    public Scope getScope() {
        try {
            return Scope.parse(tokenClaimsSet.getStringClaim(KEY_SCOPE));
        } catch (ParseException e) {
            log.error("Error parsing scope in request {}", tokenClaimsSet.getClaim(KEY_SCOPE));
            // should never happen, programming error.
            return null;
        }
    }

    /**
     * Get the id of the token.
     * 
     * @return id of the token
     */
    @Nonnull
    public String getID() {
        return tokenClaimsSet.getJWTID();
    }

    /**
     * Get Client ID of the token.
     * 
     * @return Client ID of the token
     */
    @Nonnull
    public ClientID getClientID() {
        return new ClientID((String) tokenClaimsSet.getClaim(KEY_CLIENTID));
    }

    /** Abstract builder to extend builders from that are instantiating claims sets extending TokenClaimsSet. */
    public abstract static class Builder<T extends TokenClaimsSet> {

        /** Generator for pseudo unique identifier for the claims set. */
        @Nonnull
        protected IdentifierGenerationStrategy idGen;

        /** Client Id of the rp. */
        @Nonnull
        protected ClientID rpId;

        /** OP issuer value. */
        @Nonnull
        protected String iss;

        /** User Principal of the authenticated user. */
        @Nonnull
        protected String usrPrincipal;

        /** Subject claim value of the authenticated user. */
        @Nonnull
        protected String sub;

        /** Authentication context class reference value of the authentication. */
        @Nonnull
        protected ACR acr;

        /** Issue time of the claims set. */
        @Nonnull
        protected Date iat;

        /** Expiration time of the claims set. */
        @Nonnull
        protected Date exp;

        /** Authentication time of the user. */
        @Nonnull
        protected Date authTime;

        /** Validated redirect URI of the authentication request. */
        @Nonnull
        protected URI redirect;

        /** Scope of the authentication request. */
        @Nonnull
        protected Scope reqScope;

        /** Nonce of the authentication request. */
        @Nullable
        protected Nonce nonce;

        /** Claims request of the authentication request. */
        @Nullable
        protected ClaimsRequest claims;

        /** Token delivery claims delivered both for id token and userinfo response. */
        @Nullable
        protected ClaimsSet dlClaims;

        /** Token delivery claims delivered for id token. */
        @Nullable
        protected ClaimsSet dlClaimsID;

        /** Token delivery claims delivered for userinfo response. */
        @Nullable
        protected ClaimsSet dlClaimsUI;

        /** Consentable claims. */
        @Nullable
        protected JSONArray cnsntlClaims;

        /** consented claims. */
        @Nullable
        protected JSONArray cnsntdClaims;

        /**
         * Constructor for authorize code builder.
         * 
         * @param idGenerator Generator for pseudo unique identifier for the claims set. Must not be NULL.
         * @param clientID Client Id of the rp. Must not be NULL.
         * @param issuer OP issuer value. Must not be NULL.
         * @param userPrincipal User Principal of the authenticated user. Must not be NULL.
         * @param subject subject of the authenticated user. Must not be NULL
         * @param issuedAt Issue time of the authorize code. Must not be NULL.
         * @param expiresAt Expiration time of the authorize code. Must not be NULL.
         * @param authenticationTime Authentication time of the user. Must not be NULL.
         * @param redirectURI Validated redirect URI of the authentication request. Must not be NULL.
         * @param scope Scope of the authentication request. Must not be NULL.
         */
        protected Builder(@Nonnull IdentifierGenerationStrategy idGenerator, @Nonnull ClientID clientID,
                @Nonnull String issuer, @Nonnull String userPrincipal, @Nonnull String subject, @Nonnull Date issuedAt,
                @Nonnull Date expiresAt, @Nonnull Date authenticationTime, @Nonnull URI redirectURI,
                @Nonnull Scope scope) {

            idGen = idGenerator;
            rpId = clientID;
            iss = issuer;
            usrPrincipal = userPrincipal;
            sub = subject;
            iat = issuedAt;
            exp = expiresAt;
            authTime = authenticationTime;
            redirect = redirectURI;
            reqScope = scope;

        }

        /**
         * Set authentication context class reference value of the authentication.
         * 
         * @param authenticationContextReference authentication context class reference value of the authentication.
         */
        public Builder<T> setACR(@Nullable ACR authenticationContextReference) {
            acr = authenticationContextReference;
            return this;
        }

        /**
         * Set nonce of the authentication request.
         * 
         * @param requestNonce nonce of the authentication request.
         */
        public Builder<T> setNonce(@Nullable Nonce requestNonce) {
            nonce = requestNonce;
            return this;
        }

        /**
         * Set claims request of the authentication request.
         * 
         * @param requestedClaims claims request of the authentication request.
         */
        public Builder<T> setClaims(@Nullable ClaimsRequest requestedClaims) {
            claims = requestedClaims;
            return this;
        }

        /**
         * Set token delivery claims delivered both for id token and userinfo response.
         * 
         * @param deliveryClaims token delivery claims delivered both for id token and userinfo response.
         */
        public Builder<T> setDlClaims(@Nullable ClaimsSet deliveryClaims) {
            dlClaims = deliveryClaims;
            return this;
        }

        /**
         * Set token delivery claims delivered for id token.
         * 
         * @param deliveryClaimsIDToken token delivery claims delivered for id token
         */
        public Builder<T> setDlClaimsID(@Nullable ClaimsSet deliveryClaimsIDToken) {
            dlClaimsID = deliveryClaimsIDToken;
            return this;
        }

        /**
         * Set token delivery claims delivered for userinfo response.
         * 
         * @param deliveryClaimsUserInfo token delivery claims delivered for userinfo response
         */
        public Builder<T> setDlClaimsUI(@Nullable ClaimsSet deliveryClaimsUserInfo) {
            dlClaimsUI = deliveryClaimsUserInfo;
            return this;
        }

        /**
         * Set consentable claims.
         * 
         * @param consentableClaims consentable claims
         */
        public Builder<T> setConsentableClaims(@Nullable JSONArray consentableClaims) {
            cnsntlClaims = consentableClaims;
            return this;
        }

        /**
         * Set consented claims.
         * 
         * @param consentedClaims consented claims
         */
        public Builder<T> setConsentedClaims(@Nullable JSONArray consentedClaims) {
            cnsntdClaims = consentedClaims;
            return this;
        }

        /**
         * Builds claims set.
         * 
         * @return claims set instance.
         */
        public abstract T build();

    }

}
