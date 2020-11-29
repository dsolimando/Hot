package be.solidx.hot.shows;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import be.solidx.hot.groovy.GroovyClosure;
import be.solidx.hot.groovy.GroovyMapConverter;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.js.JsMapConverter;
import be.solidx.hot.python.PyDictionaryConverter;
import be.solidx.hot.python.PythonClosure;
import be.solidx.hot.utils.*;
import com.google.common.net.HttpHeaders;
import org.mozilla.javascript.NativeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.HandlerMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dsolimando on 08/08/2018.
 *
 *  * Copyright (C) 2010 - 2018 Solidx

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 *
 */
public class RestRequestBuilderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestRequestBuilderFactory.class);

    GroovyHttpDataDeserializer groovyHttpDataDeserializer;
    PythonHttpDataDeserializer pythonHttpDataDeserializer;
    JsHttpDataDeserializer jsHttpDataDeserializer;

    GroovyMapConverter groovyMapConverter;
    PyDictionaryConverter pyDictionaryConverter;
    JsMapConverter jsMapConverter;

    @Inject
    public RestRequestBuilderFactory(GroovyHttpDataDeserializer groovyHttpDataDeserializer, PythonHttpDataDeserializer pythonHttpDataDeserializer, JsHttpDataDeserializer jsHttpDataDeserializer, GroovyMapConverter groovyMapConverter, PyDictionaryConverter pyDictionaryConverter, JsMapConverter jsMapConverter) {
        this.groovyHttpDataDeserializer = groovyHttpDataDeserializer;
        this.pythonHttpDataDeserializer = pythonHttpDataDeserializer;
        this.jsHttpDataDeserializer = jsHttpDataDeserializer;
        this.groovyMapConverter = groovyMapConverter;
        this.pyDictionaryConverter = pyDictionaryConverter;
        this.jsMapConverter = jsMapConverter;
    }

    public Instance build(ClosureRequestMapping closureRequestMapping) {
        if (closureRequestMapping.getClosure() instanceof GroovyClosure) {
            return new GroobyRequestBuilder(groovyHttpDataDeserializer, groovyMapConverter,closureRequestMapping);
        } else if (closureRequestMapping.getClosure() instanceof PythonClosure) {
            return new PythonRequestBuilder(pythonHttpDataDeserializer,pyDictionaryConverter,closureRequestMapping);
        } else if (closureRequestMapping.getClosure() instanceof JSClosure) {
            return new JSRequestBuilder(jsHttpDataDeserializer,jsMapConverter,closureRequestMapping);
        } else {
            throw new RuntimeException("showClosure is in the wrong type " + closureRequestMapping.getClosure().getClass());
        }
    }

    public static class RestRequestBuilder<T extends Map<?, ?>> implements WithBody, Builder, Authenticate, Instance {

        RestRequest<T> restRequest;

        HttpServletRequest httpServletRequest;

        HttpDataDeserializer httpDataDeserializer;

        ScriptMapConverter<T> scriptMapConverter;

        ClosureRequestMapping closureRequestMapping;

        RestRequestBuilder(HttpDataDeserializer httpDataDeserializer,
                           ScriptMapConverter scriptMapConverter,
                           ClosureRequestMapping closureRequestMapping) {
            this.httpDataDeserializer = httpDataDeserializer;
            this.scriptMapConverter = scriptMapConverter;
            this.closureRequestMapping = closureRequestMapping;
        }

        public Authenticate newRestRequest(HttpServletRequest httpServletRequest) {
            this.httpServletRequest = httpServletRequest;
            this.restRequest = new RestRequest<T>(httpServletRequest, scriptMapConverter);
            return this;
        }

        @Override
        public Builder withBody(Object body) {
            restRequest.requestBody = body;
            return this;
        }

        @Override
        public Builder withBodyConversion(byte[] body) {
            restRequest.requestBody = deserializeBody(body);
            return this;
        }

        @Override
        public RestRequest build() {

            Map<String, MultiValueMap<String, String>> matrixVariables = (Map<String, MultiValueMap<String, String>>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            if (restRequest.headers == null) {
                restRequest.headers = scriptMapConverter.httpHeadersToMap(httpServletRequest);
            }
            restRequest.pathParams = scriptMapConverter.toScriptMap(matrixVariables);
            restRequest.requestParams = scriptMapConverter.toScriptMap(httpServletRequest.getParameterMap());
            restRequest.ip = httpServletRequest.getRemoteAddr();
            restRequest.principal = buildPrincipal(httpServletRequest);
            restRequest.session = new RestRequest.Session(httpServletRequest.getSession());

            return restRequest;
        }

        @Override
        public WithBody authenticate(Authentication authentication) {

            if (authentication == null)
                return this;

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Authentication Type: "+authentication.getClass());

            Map userAsMap = new HashMap();

            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
                List<String> roles = new ArrayList<>();

                if (usernamePasswordAuthenticationToken.getPrincipal() instanceof LdapUserDetails) {
                    LdapUserDetails detailsImpl = (LdapUserDetails) usernamePasswordAuthenticationToken.getPrincipal();
                    userAsMap.put("name", detailsImpl.getUsername());
                    userAsMap.put("username", detailsImpl.getUsername());
                    userAsMap.put("password", detailsImpl.getPassword());
                    userAsMap.put("dn", detailsImpl.getDn());
                    for (GrantedAuthority authority : detailsImpl.getAuthorities()) {
                        roles.add(authority.getAuthority());
                    }
                } else {

                    Object principal = usernamePasswordAuthenticationToken.getPrincipal();
                    if (principal instanceof String) {
                        userAsMap.put("name", usernamePasswordAuthenticationToken.getPrincipal());
                        userAsMap.put("username", usernamePasswordAuthenticationToken.getPrincipal());
                        userAsMap.put("password", usernamePasswordAuthenticationToken.getCredentials());
                        for (GrantedAuthority authority : usernamePasswordAuthenticationToken.getAuthorities()) {
                            roles.add(authority.getAuthority());
                        }
                    } else if (principal instanceof User) {
                        User userDetails = (User) principal;
                        userAsMap.put("name", userDetails.getUsername());
                        userAsMap.put("username", userDetails.getUsername());
                        userAsMap.put("password", userDetails.getPassword());
                        for (GrantedAuthority authority : userDetails.getAuthorities()) {
                            roles.add(authority.getAuthority());
                        }
                    } else {
                        restRequest.user = (T) principal;
                        return this;
                    }
                }
                userAsMap.put("roles", roles);
            } else if (authentication instanceof SocialAuthenticationToken) {
                SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
                ConnectionData connectionData = socialAuthenticationToken.getConnection().createData();
                userAsMap.put("name", socialAuthenticationToken.getName());
                userAsMap.put("username", socialAuthenticationToken.getName());
                userAsMap.put("email", socialAuthenticationToken.getName());
                userAsMap.put("accessToken", connectionData.getAccessToken());
                userAsMap.put("picture", connectionData.getImageUrl());
                userAsMap.put("link", connectionData.getProfileUrl());
                userAsMap.put("id", connectionData.getProviderUserId());
                userAsMap.put("provider", connectionData.getProviderId());
                userAsMap.put("expiresIn", connectionData.getExpireTime());
            }

            restRequest.user = scriptMapConverter.toScriptMap(userAsMap);
            return this;
        }

        private RestRequest.Principal buildPrincipal(HttpServletRequest webRequest) {
            if (webRequest.getUserPrincipal() != null) {
                return new RestRequest.Principal(webRequest.getUserPrincipal().getName());
            }
            return null;
        }

        private String extractContentTypeHttpHeader () {
            for (Map.Entry<?, ?> entry : restRequest.headers.entrySet()) {
                if (entry.getKey().equals(HttpHeaders.CONTENT_TYPE)) {
                    return (String) ((List<?>)entry.getValue()).get(0);
                }
            }
            return "text/plain; charset=utf-8";
        }

        private Object deserializeBody(byte[] body) {
            if (body == null) return null;

            restRequest.headers = scriptMapConverter.httpHeadersToMap(httpServletRequest);
            String contentType = extractContentTypeHttpHeader();

            if (closureRequestMapping.options.isProcessRequestData()) {
                return httpDataDeserializer.processRequestData(body, contentType);
            } else {
                return new String(body);
            }
        }
    }

    static class GroobyRequestBuilder extends RestRequestBuilder {

        public GroobyRequestBuilder(HttpDataDeserializer httpDataDeserializer, ScriptMapConverter scriptMapConverter, ClosureRequestMapping closureRequestMapping) {
            super(httpDataDeserializer, scriptMapConverter, closureRequestMapping);
        }
    }

    static class PythonRequestBuilder extends RestRequestBuilder {

        public PythonRequestBuilder(HttpDataDeserializer httpDataDeserializer, ScriptMapConverter scriptMapConverter, ClosureRequestMapping closureRequestMapping) {
            super(httpDataDeserializer, scriptMapConverter, closureRequestMapping);
        }
    }

    static class JSRequestBuilder extends RestRequestBuilder {

        public JSRequestBuilder(HttpDataDeserializer httpDataDeserializer, ScriptMapConverter scriptMapConverter, ClosureRequestMapping closureRequestMapping) {
            super(httpDataDeserializer, scriptMapConverter, closureRequestMapping);
        }
    }

    public interface Builder {
        RestRequest build();
    }

    public interface Authenticate extends WithBody {
        WithBody authenticate(Authentication authentication);
    }

    public interface WithBody extends Builder {
        Builder withBody(Object body);
        Builder withBodyConversion(byte[] body);
    }

    public interface Instance {
        Authenticate newRestRequest(HttpServletRequest httpServletRequest);
    }
}
