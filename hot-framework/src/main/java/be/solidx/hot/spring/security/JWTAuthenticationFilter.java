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

import be.solidx.hot.spring.config.CommonConfig;
import be.solidx.hot.spring.config.HotConfig;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

public class JWTAuthenticationFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    public static final String JWT_PREFIX = "jwt";

    CommonConfig commonConfig;

    public JWTAuthenticationFilter(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;


        String authName = request.getPathInfo().substring(1);

        for (HotConfig.Auth auth : commonConfig.hotConfig().getAuthList()) {
            if (auth.getType() == HotConfig.AuthType.JWT && auth.getName().equals(authName)
                    && notAuthenticated()) {
                try {
                    DecodedJWT jwt = extractToken(request);
                    if (isValid(jwt, auth)) {
                        doAuthentication(jwt, auth, request);
                    }
                } catch (Exception e) {
                    logger.error("JWT Authentication failure", e);
                }
                break;
            }
        }
        chain.doFilter(req, res);
    }

    private boolean notAuthenticated() {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }
        return false;
    }

    private void doAuthentication(DecodedJWT jwt, HotConfig.Auth auth, HttpServletRequest request) {
        Map<String, String> user = new HashMap<>();
        user.put("id", jwt.getSubject());

        String[] claims = auth.getClaims().split(",");
        for (String claim : claims) {
            String trimmed = claim.trim();
            if (!jwt.getClaim(trimmed).isNull()) {
                user.put(trimmed, jwt.getClaim(trimmed).asString());
            }
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                "",
                Arrays.asList(new SimpleGrantedAuthority("USER"))
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authToken);
        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    private DecodedJWT extractToken(HttpServletRequest request) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.contains("Bearer")) {
            return JWT.decode(authHeader.split(" ")[1].trim());
        }
        throw new Exception("Authorization request header missing or invalid or token expired");
    }

    private boolean isValid(DecodedJWT jwt, HotConfig.Auth auth) {
        try {
            Algorithm algorithm;
            if (Arrays.asList(
                    HotConfig.Algorithm.HS256,
                    HotConfig.Algorithm.HS256,
                    HotConfig.Algorithm.HS256).contains(auth.getAlgorithm())) {
                if (auth.getSecret() != null) {
                    algorithm = getHMACAlgorithm(auth);
                } else {
                    logger.error("No secret provided for signature validation");
                    return false;
                }
            } else {
                JwkProvider jwkProvider = new UrlJwkProvider(new URL(auth.getJwksUrl()));
                Jwk jwk = jwkProvider.get(jwt.getId());
                algorithm = getAlgorithm(jwk.getPublicKey(), auth);
            }
            algorithm.verify(jwt);
            return jwt.getAudience().equals(auth.getAudience().contains(",") ? auth.getAudience().split(",") : Arrays.asList(auth.getAudience()))
                    && jwt.getExpiresAt().getTime() > System.currentTimeMillis();

        } catch (Exception e) {
            logger.error("Failed to validate JWT signature", e);
            return false;
        }
    }

    private Algorithm getHMACAlgorithm(HotConfig.Auth auth) {
        switch (auth.getAlgorithm()) {
            default:
            case HS256:
                return Algorithm.HMAC256(auth.getSecret());
            case HS384:
                return Algorithm.HMAC384(auth.getSecret());
            case HS512:
                return Algorithm.HMAC512(auth.getSecret());
        }
    }

    private Algorithm getAlgorithm(PublicKey pkey, HotConfig.Auth auth) {
        switch (auth.getAlgorithm()) {
            default:
            case RS256:
                return Algorithm.RSA256((RSAPublicKey) pkey, null);
            case RS384:
                return Algorithm.RSA384((RSAPublicKey) pkey, null);
            case RS512:
                return Algorithm.RSA512((RSAPublicKey) pkey, null);
            case ES256:
                return Algorithm.ECDSA256((ECPublicKey) pkey, null);
            case ES384:
                return Algorithm.ECDSA384((ECPublicKey) pkey, null);
            case ES512:
                return Algorithm.ECDSA512((ECPublicKey) pkey, null);
        }
    }
}
