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

package org.geant.idpextension.oidc.encoding.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;

class ResponseUtil {

    /**
     * Helper method to print response to string for logging.
     * 
     * @param httpResponse response to be printed
     * @return response as formatted string.
     */
    protected static String toString(HTTPResponse httpResponse) {
        if (httpResponse == null) {
            return null;
        }
        String nl = System.lineSeparator();
        String ret = nl;
        Map<String, List<String>> headers = httpResponse.getHeaderMap();
        if (headers != null) {
            ret += "Headers:" + nl;
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                ret += "\t" + entry.getKey() + ":" + entry.getValue().get(0) + nl;
            }
        }
        if (httpResponse.getContent() != null) {
            ret += "Content:" + httpResponse.getContent();
        }
        return ret;
    }

    /**
     * Helper method to print response to string for logging.
     * 
     * @param httpServletResponse response to be printed
     * @return response as formatted string.
     */
    protected static String toString(HttpServletResponse httpServletResponse, String content) {
        if (httpServletResponse == null) {
            return null;
        }
        String nl = System.lineSeparator();
        String ret = nl;
        Collection<String> headerNames = httpServletResponse.getHeaderNames();
        if (headerNames != null) {
            ret += "Headers:" + nl;
            for (String headerName : headerNames) {
                ret += "\t" + headerName + ":" + httpServletResponse.getHeader(headerName) + nl;
            }
        }
        if (content != null) {
            ret += "Content:" + content;
        }
        return ret;
    }

}
