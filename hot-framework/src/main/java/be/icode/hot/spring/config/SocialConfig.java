package be.icode.hot.spring.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

import be.icode.hot.spring.config.HotConfig.Auth;
import be.icode.hot.spring.config.HotConfig.AuthType;

@Configuration
@EnableSocial
@Import({CommonConfig.class})
public class SocialConfig implements SocialConfigurer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SocialConfig.class);

	@Inject
	CommonConfig commonConfig;
	
	@Override
	public void addConnectionFactories(ConnectionFactoryConfigurer configurer, Environment env) {
		try {
			boolean facebook = false, 
					google = false;
			for (Auth auth: commonConfig.hotConfig().getAuthList()) {
				
				if ((auth.getType() == AuthType.FACEBOOK || auth.getType() == AuthType.FACEBOOK_CLIENT) && !facebook) {
					configurer.addConnectionFactory(new FacebookConnectionFactory(auth.getConsumerKey(), auth.getConsumerSecret()));
					facebook = true;
				} else if (auth.getType() == AuthType.TWITTER) {
					configurer.addConnectionFactory(new TwitterConnectionFactory(auth.getConsumerKey(), auth.getConsumerSecret()));
				} else if ((auth.getType() == AuthType.GOOGLE || auth.getType() == AuthType.GOOGLE_CLIENT) && !google) {
					configurer.addConnectionFactory(new GoogleConnectionFactory(auth.getConsumerKey(), auth.getConsumerSecret()){
						@Override
						public String getScope() {
							return "profile";
						}
					});
					google = true;
				}
			}
		} catch ( IOException e) {
			LOGGER.error("",e);
		}
	}

	@Override
	public UserIdSource getUserIdSource() {
		return new AuthenticationNameUserIdSource();
	}
	
	@Override
	public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator	) {
		
		InMemoryUsersConnectionRepository connectionRepository = new InMemoryUsersConnectionRepository(connectionFactoryLocator);
		
		connectionRepository.setConnectionSignUp(new ConnectionSignUp() {
			
			List<String> usernames = new ArrayList<>();
			
			@Override
			public String execute(Connection<?> connection) {
				 UserProfile profile = connection.fetchUserProfile();
				 usernames.add(profile.getUsername()==null?profile.getEmail():profile.getUsername());
				 return profile.getUsername()==null?profile.getEmail():profile.getUsername();
			}
		});
		return connectionRepository;
	}
	
	@Bean
	public SocialUserDetailsService socialUserDetailsService() {
		return new SocialUserDetailsService() {
			
			@Override
			public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
				return new SocialUser(userId, "", new ArrayList<GrantedAuthority>());
			}
		};
	}
}
