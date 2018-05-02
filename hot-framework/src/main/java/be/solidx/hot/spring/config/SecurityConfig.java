package be.solidx.hot.spring.config;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer.UserDetailsBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;

import com.google.common.net.HttpHeaders;

import be.solidx.hot.data.jdbc.AbstractDB;
import be.solidx.hot.data.jdbc.DB;
import be.solidx.hot.data.mongo.BasicDB;
import be.solidx.hot.shows.ClosureRequestMapping;
import be.solidx.hot.shows.Show;
import be.solidx.hot.shows.rest.HotContext;
import be.solidx.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.solidx.hot.spring.config.HotConfig.Auth;
import be.solidx.hot.spring.config.HotConfig.AuthType;
import be.solidx.hot.spring.config.HotConfig.DBEngine;
import be.solidx.hot.spring.config.HotConfig.DataSource;
import be.solidx.hot.spring.security.OAuth2ClientAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Import({CommonConfig.class, ShowConfig.class, DataConfig.class})
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);
	// auth-db -name ds 
	// auth-ldap -server ldap://springframework.org:389/dc=springframework,dc=org -user-dn-pattern uid={0},ou=people -group-search-base ou=groups
	// auth-ldap -server ldap://springframework.org:389/dc=springframework,dc=org --ldap-user-search-filter uid={0} --ldap-user-search-base ou=people
	// auth-facebook -dbname db 
	// auth-twitter -dbname db 
	// auth-google -dbname db 
	// auth-client-facebook -dbname db
	// auth-client-google -dbname db
	
	@Autowired
	CommonConfig commonConfig;
	
	@Autowired
	ShowConfig showConfig;
	
	@Autowired
	DataConfig dataConfig;
	
	@Autowired
	UsersConnectionRepository usersConnectionRepository;
	
	@Autowired
	SocialAuthenticationServiceLocator authServiceLocator;
	
	@Autowired
	UserIdSource userIdSource;
	
	@Autowired
	SocialUserDetailsService socialUserDetailsService;
	
	@Autowired
	protected void authenticationManager (AuthenticationManagerBuilder auth) throws Exception {
		
		AuthenticationManagerBuilder and = auth;
		
		boolean social = false;
		
		for (Auth hotauth: commonConfig.hotConfig().getAuthList()) {
			if (hotauth.dbname != null && !hotauth.dbname.isEmpty()) {
				for (Entry<String, be.solidx.hot.data.DB<Map<String, Object>>> entry : dataConfig.groovyDbMap().entrySet()) {
					
					
					if (entry.getKey().equals(hotauth.dbname)) {
						
						if (entry.getValue() instanceof BasicDB) {
							and.userDetailsService(hotMongoUserDetailsService(entry.getValue(), hotauth)).passwordEncoder(passwordEncoder());
							
							if (hotauth.dbDefaultUsername != null && !hotauth.dbDefaultUsername.isEmpty()
									&& hotauth.dbDefaultPassword != null && !hotauth.dbDefaultPassword.isEmpty()) {
								
								// if user doesn't exist, we create it
								Map<String, Object> query = new HashMap<>();
								query.put("username", hotauth.dbDefaultUsername);
								
								if (entry.getValue().getCollection("users").findOne(query) == null) {

									Map<String, Object> user  = new HashMap<>();
									user.put("username", hotauth.dbDefaultUsername);
									user.put("password", passwordEncoder().encode(hotauth.dbDefaultUsername));
									user.put("enabled", true);
									
									if (!hotauth.dbDefaultRoles.isEmpty() && !hotauth.isWithGroups()) {
										user.put("authorities", hotauth.dbDefaultRoles);
									}
									
									entry.getValue().getCollection("users").insert(user);
								}
							}
						} else {
							DataSource authDataSource = null;
							for (DataSource datasource : commonConfig.hotConfig().getDataSources()) {
								if (datasource.getName().equals(hotauth.dbname)) {
									authDataSource = datasource;
									break;
								}
							}
									
							AbstractDB<?> abstractDB = (AbstractDB<?>) entry.getValue(); 
							JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer = and.jdbcAuthentication().dataSource(abstractDB.getDataSource());
							
							if (hotauth.isWithGroups()) {
								configurer.getUserDetailsService().setEnableGroups(true);
							}
							
							configurer.passwordEncoder(passwordEncoder());
							
							DB<Map<String, Object>> db = (DB<Map<String, Object>>) dataConfig.groovyDbMap().get(entry.getKey());
							
							// If embedded H2 or HSQLDB and security schema not already created, we create it.
							if (authDataSource.getEngine() == DBEngine.HSQLDB
									|| authDataSource.getEngine() == DBEngine.H2) {
								
								if (!db.listCollections().contains("users")) {
									configurer = configurer.withDefaultSchema();
								}
							}
							
							if (hotauth.dbDefaultUsername != null && !hotauth.dbDefaultUsername.isEmpty()
									&& hotauth.dbDefaultPassword != null && !hotauth.dbDefaultPassword.isEmpty()) {
								
								Map<String, Object> query = new HashMap<>();
								query.put("username", hotauth.dbDefaultUsername);
								if (!db.listCollections().contains("users") || db.getCollection("users").findOne(query) == null) {
									UserDetailsBuilder userDetailsBuilder = configurer.withUser(hotauth.dbDefaultUsername).password(passwordEncoder().encode(hotauth.dbDefaultPassword));
									
									if (!hotauth.dbDefaultRoles.isEmpty()) {
										userDetailsBuilder = userDetailsBuilder.authorities(hotauth.dbDefaultRoles.toArray(new String[]{}));
									}
								}
							}
							and = configurer.and();
							break;
						}
					}
				}
			} else if (hotauth.ldapServerUrl != null && !hotauth.ldapServerUrl.isEmpty()) {
				LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> ldapConfigurer = and.ldapAuthentication()
						.contextSource().url(hotauth.ldapServerUrl).and();
				
				if (hotauth.userDnPatterns != null && !(hotauth.userDnPatterns.length == 0)) {
					ldapConfigurer = ldapConfigurer.userDnPatterns(hotauth.userDnPatterns);
				} else {
					if (hotauth.userSearchBase != null && !hotauth.userSearchBase.isEmpty()) {
						ldapConfigurer = ldapConfigurer.userSearchBase(hotauth.userSearchBase);
					}
					if (hotauth.userSearchFilter != null && !hotauth.userSearchFilter.isEmpty()) {
						ldapConfigurer = ldapConfigurer.userSearchFilter(hotauth.userSearchFilter);
					}
				}
				if (hotauth.groupSearchBase != null && !hotauth.groupSearchBase.isEmpty()) {
					ldapConfigurer = ldapConfigurer.groupSearchBase(hotauth.groupSearchBase);
				}
				if ( hotauth.groupSearchFilter != null && !hotauth.groupSearchFilter.isEmpty()) {
					ldapConfigurer = ldapConfigurer.groupSearchFilter(hotauth.groupSearchFilter);
				}
			}
			
			if ((hotauth.getType() == AuthType.FACEBOOK
					|| hotauth.getType() == AuthType.TWITTER
					|| hotauth.getType() == AuthType.GOOGLE
					|| hotauth.getType() == AuthType.FACEBOOK_CLIENT
					|| hotauth.getType() == AuthType.GOOGLE_CLIENT) && !social) {
				and.authenticationProvider(new SocialAuthenticationProvider(usersConnectionRepository, socialUserDetailsService));
				social = true;
			}
		}
	}
	
	@Configuration
	@Order(1)
	public static class StaticResourcesConfig extends WebSecurityConfigurerAdapter {
		
		@Autowired
		CommonConfig commonConfig;
		
		@Autowired
		SecurityConfig securityConfig;
		
		@Autowired
		OAuth2ClientAuthenticationFilter facebookClientAuthenticationFilter;
		
		@Autowired
		SpringSocialConfigurer springSocialConfigurer;
		
		
		@Override
		public void configure(WebSecurity web) throws Exception {
			
			if (commonConfig.secureDirs().isEmpty()) return;
			
			IgnoredRequestConfigurer configurer = web.ignoring();
			
			for (String path : commonConfig.securityBypassDirs()) {
				configurer.antMatchers(path+"/*.*");
			}
		}
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			
			HttpSecurity and = http.requestMatcher(new RequestMatcher() {
				@Override
				public boolean matches(HttpServletRequest request) {
					return !request.getRequestURI().startsWith("/rest") && !request.getRequestURI().startsWith("/client-auth") && !request.getRequestURI().startsWith("/rest-login");
				}
			}).csrf().disable();
			
			if (commonConfig.secureDirs().size() == 0)
				return;
			
			for (String path : commonConfig.secureDirs()) {
				and = and.authorizeRequests().antMatchers(path+"/*").authenticated().and();
			}
			
			if (getClass().getResource("/login.html") != null || getClass().getResource("/www/login.html") != null) {
				and = and.formLogin().loginPage("/login.html").permitAll().loginProcessingUrl("/login").and();
			} else {
				and = and.formLogin().and();
			}
			
			for (Auth auth: commonConfig.hotConfig().getAuthList()) {
				if ((auth.getType() == AuthType.FACEBOOK
						|| auth.getType() == AuthType.TWITTER
						|| auth.getType() == AuthType.GOOGLE)) {

					and = and.apply(springSocialConfigurer).and();
					break;
				}
			}
		}
	}
	
	@Configuration
	@Order(2)
	public static class ClientAuthConfig extends WebSecurityConfigurerAdapter {
		
		@Autowired
		OAuth2ClientAuthenticationFilter oAuth2ClientAuthenticationFilter;
		
		@Autowired
		CommonConfig commonConfig;
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			
			HttpSecurity and = http.requestMatcher(new RequestMatcher() {
				@Override
				public boolean matches(HttpServletRequest request) {
					return request.getRequestURI().startsWith("/client-auth");
				}
			});
			
			for (Auth auth: commonConfig.hotConfig().getAuthList()) {
				if (auth.getType() == AuthType.FACEBOOK_CLIENT || auth.getType() == AuthType.GOOGLE_CLIENT) {
					and.addFilterBefore(oAuth2ClientAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class)
					.formLogin().disable()
					.csrf().disable()
					.httpBasic().disable()
					.jee().disable();
					break;
				}
			}
		}
	}
	
	@Configuration
	@Order(3)
	public static class HtmlRestShowsConfig extends WebSecurityConfigurerAdapter {
		
		@Autowired
		ShowConfig showConfig;
		
		@Autowired
		CommonConfig commonConfig;
		
		@Autowired
		SpringSocialConfigurer springSocialConfigurer;
		
		@Autowired
		ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;
		
		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring().requestMatchers(new AuthRequestMatcher(closureRequestMappingHandlerMapping));
		}
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			
			HttpSecurity and = http.requestMatcher(new RequestMatcher() {
				@Override
				public boolean matches(HttpServletRequest request) {
					return request.getHeader(HttpHeaders.ACCEPT) != null 
							&& request.getHeader(HttpHeaders.ACCEPT).contains("text/html,application/xhtml")
							&& request.getRequestURI().startsWith("/rest");
				}
			})
			.csrf().disable()
			.headers().cacheControl().disable();
			
			boolean hasauth = false;
			String dynamicLoginPage = null;
			
			String loginPage = null;
			
			if (getClass().getResource("/login.html") != null){
				loginPage = "/login.html";
			} else if (getClass().getResource("/www/login.html") != null) {
				loginPage = "/www/login.html";
			}
			
			for (Show<?,?> show : showConfig.showsContext().getShows()) {
				for (ClosureRequestMapping requestMapping :show.getRest().getRequestMappings()) {
					if (requestMapping.isAuth()) {
						hasauth = true;
						List<String> paths = new ArrayList<>();
						for (String path : requestMapping.getPaths()) {
								paths.add("/rest"+path);
								
						}
						String[] rolesArray = requestMapping.getRoles();
						if (rolesArray.length > 0) {
							and = and.authorizeRequests()
									.antMatchers(
											HttpMethod.valueOf(requestMapping.getRequestMethod().name()), 
											paths.toArray(new String[]{}))
									.hasAnyRole(rolesArray).and();
							
						} else {
							and = and.authorizeRequests()
									.antMatchers(
											HttpMethod.valueOf(requestMapping.getRequestMethod().name()), 
											paths.toArray(new String[]{}))
									.authenticated()
									.and();
						}
					} else {
						for (String path : requestMapping.getPaths()) {
							if (path.startsWith("/login")) {
								dynamicLoginPage = "/rest"+path;
								break;
							}
						}
					}
				}
			}
			
			if (hasauth && dynamicLoginPage != null) {
				and.formLogin().loginPage(dynamicLoginPage).permitAll().loginProcessingUrl("/rest-login").and();
			} else if (hasauth && loginPage != null) {
				and.formLogin().loginPage(loginPage).permitAll().loginProcessingUrl("/rest-login").and();
			} else if (hasauth) {
				and.formLogin().loginProcessingUrl("/rest-login").and();
			}
			
			for (Auth auth: commonConfig.hotConfig().getAuthList()) {
				if ((auth.getType() == AuthType.FACEBOOK
						|| auth.getType() == AuthType.TWITTER
						|| auth.getType() == AuthType.GOOGLE)) {
					and = and.apply(springSocialConfigurer).and();
					break;
				}
			}
		}
	}
	
	@Configuration
	@Order(4)
	public static class RestShowsConfig extends WebSecurityConfigurerAdapter {
		
		@Autowired
		ShowConfig showConfig;
		
		@Autowired
		CommonConfig commonConfig;
		
		@Autowired
		SpringSocialConfigurer springSocialConfigurer;
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			
			HttpSecurity and = http.antMatcher("/rest/**").httpBasic().and().csrf().disable().headers().cacheControl().disable();
			
			for (Show<?,?> show : showConfig.showsContext().getShows()) {
				for (ClosureRequestMapping requestMapping :show.getRest().getRequestMappings()) {
					if (requestMapping.isAuth()) {
						
						List<String> paths = new ArrayList<>();
						for (String path : requestMapping.getPaths()) {
							paths.add("/rest"+path);
							paths.add("/rest"+path+'/');
						}
						if (requestMapping.getRoles().length > 0) {
							and = and.authorizeRequests()
									.antMatchers(
											HttpMethod.valueOf(requestMapping.getRequestMethod().name()), 
											paths.toArray(new String[]{}))
									.hasAnyRole(requestMapping.getRoles()).and();
						} else {
							and = and.authorizeRequests()
									.antMatchers(
											HttpMethod.valueOf(requestMapping.getRequestMethod().name()), 
											paths.toArray(new String[]{}))
									.authenticated().and();
						}
					} 
				}
			}
			for (Auth auth: commonConfig.hotConfig().getAuthList()) {
				if ((auth.getType() == AuthType.FACEBOOK
						|| auth.getType() == AuthType.TWITTER
						|| auth.getType() == AuthType.GOOGLE)) {
					and = and.apply(springSocialConfigurer).and();
					break;
				}
			}
		}
	}
	
	private static class AuthRequestMatcher implements RequestMatcher {
		
		ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;

		public AuthRequestMatcher(ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping) {
			this.closureRequestMappingHandlerMapping = closureRequestMappingHandlerMapping;
		}

		@Override
		public boolean matches(HttpServletRequest request) {
			try {
				final ClosureRequestMapping closureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(request);
				if (closureRequestMapping != null) {
					HotContext.setRequestMapping(closureRequestMapping);
					if (!closureRequestMapping.isAnonymous() && !closureRequestMapping.isAuth()) {
						return true;
					}
				} else {
					HotContext.setRequestMapping(null);
				}
				return false;
			} catch (Exception e) {
				LOGGER.error("Failed to lookup the closure request",e);
				return false;
			}
		}
	}
	
	@Bean
	public SpringSocialConfigurer socialConfigurer() throws JsonParseException, JsonMappingException, IOException {
		for (Auth auth: commonConfig.hotConfig().getAuthList()) {
			if ((auth.getType() == AuthType.FACEBOOK
					|| auth.getType() == AuthType.TWITTER
					|| auth.getType() == AuthType.GOOGLE
					|| auth.getType() == AuthType.FACEBOOK_CLIENT
					|| auth.getType() == AuthType.GOOGLE_CLIENT)) {
				return new SpringSocialConfigurer();
			}
		}
		return null;
	}
	
	@Bean
	public OAuth2ClientAuthenticationFilter oAuth2ClientAuthenticationFilter() throws Exception {
		return new OAuth2ClientAuthenticationFilter(
				authenticationManager(), 
				userIdSource, 
				usersConnectionRepository, 
				authServiceLocator);
	}

	public UserDetailsService hotMongoUserDetailsService (final be.solidx.hot.data.DB<Map<String, Object>> db, final Auth auth) {
		
		return new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				if (auth.withGroups) {
					Map<String, Object> query = new HashMap<>();
					query.put("username", username);
					
					final Map<String, Object> res = db.getCollection("users").findOne(query);
					Set<GrantedAuthority> grantedAuthorities = getUserAuthoritiesFromGroup(username);
					
					return createUser(res, grantedAuthorities);
				} else {
					return getUserWithAuthorities(username);
				}
			}
			
			private User createUser (Map<String, Object> res, Set<GrantedAuthority> grantedAuthorities) {
				boolean enabled = true;
				if (res.get("enabled") != null && res.get("enabled") instanceof Boolean) {
					enabled = (Boolean)res.get("enabled");
				}
				return new User(res.get("username").toString(), res.get("password").toString(), enabled, true, true, true, grantedAuthorities);
			
			}
			
			private User getUserWithAuthorities (String username) {
				
				Map<String, Object> query = new HashMap<>();
				query.put("username", username);
				
				Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
				
				final Map<String, Object> res = db.getCollection("users").findOne(query);
				
				Object authorities = res.get("authorities");
				if (authorities != null && authorities instanceof List<?>) {
					for (Object authority : (List<?>)res.get("authorities")) {
						grantedAuthorities.add(new SimpleGrantedAuthority(authority.toString()));
					}
				}
				return createUser(res, grantedAuthorities);
			}
			
			private Set<GrantedAuthority> getUserAuthoritiesFromGroup(String username) {
				
				Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
				
				Map<String, Object> userQuery = new HashMap<>();
				userQuery.put("username", username);
				
				Map<String, Object> groupQuery = new HashMap<>();
				
				final Map<String, Object> res = db.getCollection("users").findOne(userQuery);
				
				Map<String, Object> inQuery = new HashMap<>();
				inQuery.put("$in", res.get("groups"));
				groupQuery.put("name", inQuery);
				
				for (Map<String, Object> group : db.getCollection("groups").find(groupQuery)) {
					List<GrantedAuthority> authorities = new ArrayList<>();
					for (String role : (List<String>)group.get("authorities")) {
						authorities.add(new SimpleGrantedAuthority(role));
					}
					grantedAuthorities.addAll(authorities);
				};
				return grantedAuthorities;
			}
		};
	}
	
	@Bean
	public PasswordEncoder passwordEncoder () {
		return new BCryptPasswordEncoder();
	}
	
	
//	public UserDetailsService hotJdbcUserDetailsService(final DB<Map<String, Object>> db, final Auth auth) {
//		
//		return new UserDetailsService() {
//			
//			@Override
//			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//				if (auth.withGroups) {
//					Map<String, Object> query = new HashMap<>();
//					query.put("username", username);
//					
//					final Map<String, Object> res = db.getCollection("users").findOne(query);
//					Set<GrantedAuthority> grantedAuthorities = getUserAuthoritiesFromGroup(username);
//					
//					return new User(res.get("username").toString(), res.get("password").toString(), true, true, true, true, grantedAuthorities);
//				} else return getUserWithAuthorities(username);
//			}
//			
//			private User getUserWithAuthorities (String username) {
//				
//				Map<String, Object> query = new HashMap<>();
//				query.put("username", username);
//				
//				Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
//				
//				final Map<String, Object> res = db.getCollection("users").join(Arrays.asList("authorities")).findOne(query);
//				
//				for (Object o : (List<?>)res.get("authorities")) {
//					Map<String,Object> authority = (Map<String,Object>) o;
//					grantedAuthorities.add(new SimpleGrantedAuthority(authority.get("authority").toString()));
//				}
//				return new User(res.get("username").toString(), res.get("password").toString(), true, true, true, true, grantedAuthorities);
//			}
//			
//			private Set<GrantedAuthority> getUserAuthoritiesFromGroup(String username) {
//				
//				Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
//				Map<String, Object> query = new HashMap<>();
//				query.put("group_members.username", username);
//				for (Map<String, Object> it : db.getCollection("groups").join(Arrays.asList("group_authorities","group_members")).find(query)) {
//					for (Map<String, Object> grAuthority : (List<Map<String, Object>>)it.get("group_authorities")) {
//						grantedAuthorities.add(new SimpleGrantedAuthority(grAuthority.get("authority").toString()));
//					}
//				}
//				return grantedAuthorities;
//			}
//		};
//	}
}
