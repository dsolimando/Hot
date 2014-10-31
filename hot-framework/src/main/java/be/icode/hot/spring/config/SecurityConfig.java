package be.icode.hot.spring.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;

import be.icode.hot.data.jdbc.AbstractDB;
import be.icode.hot.data.jdbc.DB;
import be.icode.hot.data.mongo.BasicDB;
import be.icode.hot.shows.ClosureRequestMapping;
import be.icode.hot.shows.Show;
import be.icode.hot.spring.config.HotConfig.Auth;
import be.icode.hot.spring.config.HotConfig.AuthType;
import be.icode.hot.spring.config.HotConfig.DBEngine;
import be.icode.hot.spring.config.HotConfig.DataSource;
import be.icode.hot.spring.security.OAuth2ClientAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Import({CommonConfig.class, ShowConfig.class, DataConfig.class})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
				for (Entry<String, be.icode.hot.data.DB<Map<String, Object>>> entry : dataConfig.groovyDbMap().entrySet()) {
					
					
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
				LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> ldapConfigurer = and.ldapAuthentication().contextSource().url(hotauth.ldapServerUrl).and();
				
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
	@Order(3)
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
		protected void configure(HttpSecurity http) throws Exception {
			
			HttpSecurity and = http.csrf().disable();
			
			for (String path : commonConfig.secureDirs()) {
				and = and.authorizeRequests().antMatchers(path+"/*").authenticated().and();
			}
			and = and.formLogin().and();
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
			
			HttpSecurity and = http.antMatcher("/client-auth");
			
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
	@Order(1)
	public static class RestShowsConfig extends WebSecurityConfigurerAdapter {
		
		@Autowired
		ShowConfig showConfig;
		
		@Autowired
		CommonConfig commonConfig;
		
		@Autowired
		SpringSocialConfigurer springSocialConfigurer;
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			
			HttpSecurity and = http.antMatcher("/rest/**").httpBasic().and().csrf().disable();
			
			and = and.headers().cacheControl().disable();
			
			for (Show<?,?> show : showConfig.showsContext().getShows()) {
				for (ClosureRequestMapping requestMapping :show.getRest().getRequestMappings()) {
					if (requestMapping.isAuth()) {
						
						List<String> paths = new ArrayList<>();
						for (String path : requestMapping.getPaths()) {
							paths.add("/rest"+path);
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

	public UserDetailsService hotMongoUserDetailsService (final be.icode.hot.data.DB<Map<String, Object>> db, final Auth auth) {
		
		return new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				if (auth.withGroups) {
					Map<String, Object> query = new HashMap<>();
					query.put("username", username);
					
					final Map<String, Object> res = db.getCollection("users").findOne(query);
					Set<GrantedAuthority> grantedAuthorities = getUserAuthoritiesFromGroup(username);
					
					return new User(res.get("username").toString(), res.get("password").toString(), true, true, true, true, grantedAuthorities);
				} else {
					return getUserWithAuthorities(username);
				}
			}
			
			private User getUserWithAuthorities (String username) {
				
				Map<String, Object> query = new HashMap<>();
				query.put("username", username);
				
				Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
				
				final Map<String, Object> res = db.getCollection("users").findOne(query);
				
				for (Object authority : (List<?>)res.get("authorities")) {
					grantedAuthorities.add(new SimpleGrantedAuthority(authority.toString()));
				}
				return new User(res.get("username").toString(), res.get("password").toString(), true, true, true, true, grantedAuthorities);
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
