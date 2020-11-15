package be.solidx.hot.spring.security;

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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import be.solidx.hot.spring.config.CommonConfig;
import be.solidx.hot.spring.config.HotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.security.SocialAuthenticationRedirectException;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.provider.SocialAuthenticationService;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

public class OAuth2ClientAuthenticationFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2ClientAuthenticationFilter.class);

    public static final String IS_ACCESS_TOKEN_VALID = "is_access_token_valid";

    private SocialAuthenticationServiceLocator authServiceLocator;

    private String connectionAddedRedirectUrl = "/";

    private boolean updateConnections = true;

    private UserIdSource userIdSource;

    private UsersConnectionRepository usersConnectionRepository;

    private AuthenticationManager authenticationManager;

    protected AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    protected HotConfig hotConfig;

    public OAuth2ClientAuthenticationFilter(
            AuthenticationManager authManager,
            UserIdSource userIdSource,
            UsersConnectionRepository usersConnectionRepository,
            SocialAuthenticationServiceLocator authServiceLocator,
            HotConfig hotConfig) {
        this.authenticationManager = authManager;
        this.userIdSource = userIdSource;
        this.usersConnectionRepository = usersConnectionRepository;
        this.authServiceLocator = authServiceLocator;
        this.hotConfig = hotConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String provider = req.getParameter("provider");
        if (provider == null || provider.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("no provider type provided by client, provider parameter missing in URL");
            chain.doFilter(request, response);
            return;
        }

        if (!authenticationIsRequired()) {
            chain.doFilter(request, response);
        } else {
            Authentication auth = attemptAuthService(provider, request, response);
            if (auth == null) {
                if (logger.isDebugEnabled())
                    logger.debug("Authentication failed...");
                chain.doFilter(request, response);
            } else {
                SecurityContextHolder.getContext().setAuthentication(auth);
                chain.doFilter(request, response);
            }
        }
    }


    // private helpers
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /*
     * Call SocialAuthenticationService.getAuthToken() to get SocialAuthenticationToken:
     *     If first phase, throw AuthenticationRedirectException to redirect to provider website.
     *     If second phase, get token/code from request parameter and call provider API to get accessToken/accessGrant.
     * Check Authentication object in spring security context, if null or not authenticated,  call doAuthentication()
     * Otherwise, it is already authenticated, add this connection.
     */
    private Authentication attemptAuthService(String provider, final HttpServletRequest request, HttpServletResponse response)
            throws SocialAuthenticationRedirectException, AuthenticationException {

        String accessToken = request.getParameter("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("no oauth access_token provided by client, access_token parameter missing in URL");
            return null;
        }

        if (provider.equals("facebook")) {
            if (!isValidFacebookAccessToken(accessToken)) {
                request.getSession(true).setAttribute(IS_ACCESS_TOKEN_VALID,false);
                return null;
            }
            request.getSession(true).setAttribute(IS_ACCESS_TOKEN_VALID,true);
        }

        SocialAuthenticationService<?> authService;
        // 6140 default facebook expiration time
        AccessGrant accessGrant = new AccessGrant(accessToken, null, null, 6140l);
        Connection<?> connection = null;
        if (provider.equals("twitter")) {
            logger.debug("twitter client OAuth2 authentication not yet implemented");
            return null;
        } else {
            authService = authServiceLocator.getAuthenticationService(provider);
            connection = ((OAuth2ConnectionFactory) authService.getConnectionFactory()).createConnection(accessGrant);
        }

        final SocialAuthenticationToken token = new SocialAuthenticationToken(connection, null);

        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return doAuthentication(authService, request, token);
        } else {
            addConnection(authService, request, token, auth);
            return null;
        }
    }

    private boolean authenticationIsRequired() {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }
        return false;
    }

    private void addConnection(final SocialAuthenticationService<?> authService, HttpServletRequest request, SocialAuthenticationToken token, Authentication auth) {
        // already authenticated - add connection instead
        String userId = userIdSource.getUserId();
        Object principal = token.getPrincipal();
        if (userId == null || !(principal instanceof ConnectionData)) return;

        Connection<?> connection = addConnection(authService, userId, (ConnectionData) principal);
        if (connection != null) {
            String redirectUrl = authService.getConnectionAddedRedirectUrl(request, connection);
            if (redirectUrl == null) {
                // use default instead
                redirectUrl = connectionAddedRedirectUrl;
            }
            throw new SocialAuthenticationRedirectException(redirectUrl);
        }
    }

    private Authentication doAuthentication(SocialAuthenticationService<?> authService, HttpServletRequest request, SocialAuthenticationToken token) {
        try {
            if (!authService.getConnectionCardinality().isAuthenticatePossible()) return null;
            token.setDetails(authenticationDetailsSource.buildDetails(request));
            Authentication success = this.authenticationManager.authenticate(token);
            Assert.isInstanceOf(SocialUserDetails.class, success.getPrincipal(), "unexpected principle type");
            updateConnections(authService, token, success);
            return success;
        } catch (BadCredentialsException e) {
            throw e;
        }
    }


    private void updateConnections(SocialAuthenticationService<?> authService, SocialAuthenticationToken token, Authentication success) {
        if (updateConnections) {
            String userId = ((SocialUserDetails) success.getPrincipal()).getUserId();
            Connection<?> connection = token.getConnection();
            ConnectionRepository repo = usersConnectionRepository.createConnectionRepository(userId);
            repo.updateConnection(connection);
        }
    }

    protected Connection<?> addConnection(SocialAuthenticationService<?> authService, String userId, ConnectionData data) {
        HashSet<String> userIdSet = new HashSet<String>();
        userIdSet.add(data.getProviderUserId());
        Set<String> connectedUserIds = usersConnectionRepository.findUserIdsConnectedTo(data.getProviderId(), userIdSet);
        if (connectedUserIds.contains(userId)) {
            // already connected
            return null;
        } else if (!authService.getConnectionCardinality().isMultiUserId() && !connectedUserIds.isEmpty()) {
            return null;
        }

        ConnectionRepository repo = usersConnectionRepository.createConnectionRepository(userId);

        if (!authService.getConnectionCardinality().isMultiProviderUserId()) {
            List<Connection<?>> connections = repo.findConnections(data.getProviderId());
            if (!connections.isEmpty()) {
                // TODO maybe throw an exception to allow UI feedback?
                return null;
            }
        }

        // add new connection
        Connection<?> connection = authService.getConnectionFactory().createConnection(data);
        connection.sync();
        repo.addConnection(connection);
        return connection;
    }

    protected boolean isValidFacebookAccessToken(String accessToken) {
        try {
            String consumerSecret = null;
            String consumerKey = null;
            for (HotConfig.Auth auth : hotConfig.getAuthList()) {
                if (auth.getType() == HotConfig.AuthType.FACEBOOK) {
                    consumerSecret = auth.getConsumerSecret();
                    consumerKey = auth.getConsumerKey();
                }
            }
            if (consumerSecret == null) {
                LOGGER.error(String.format("App ID missing for proget %s", this.hotConfig.getName()));
                return false;
            }

            RestTemplate rs = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s|%s", consumerKey, consumerSecret));
            RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, new URI(String.format("https://graph.facebook.com/debug_token?input_token=%s", accessToken)));
            ResponseEntity<Map> response = rs.exchange(requestEntity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            }
            LOGGER.error("The App_id in the input_token did not match the Viewing App");
            return false;
        } catch (HttpClientErrorException | URISyntaxException e) {
            LOGGER.error("", e);
            return false;
        }
    }
}
