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

import java.util.ArrayList;
import java.util.List;

public class HotConfig {
	
	public static final String GAE = "gae";

	private String name;
	
	private String nature;
	
	private String version;
	
	private boolean devMode;
	
	private boolean ssl;
	
	private String keystorePassword;
	
	private List<DataSource> dataSources = new ArrayList<HotConfig.DataSource>();
	
	private List<Auth> authList = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public List<DataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<DataSource> dataSources) {
		this.dataSources = dataSources;
	}
	
	public List<Auth> getAuthList() {
		return authList;
	}

	public void setAuthList(List<Auth> authList) {
		this.authList = authList;
	}

	public boolean isDevMode() {
		return devMode;
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	
	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}
	
	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public static class DataSource {
		
		private String name;
		
		private DBEngine engine;
		
		private String hostname;
		
		private int port;
		
		private String username;
		
		private String password;
		
		private String database;
		
		private String schema;
		
		private boolean rest;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public DBEngine getEngine() {
			return engine;
		}
		
		public void setEngine(DBEngine engine) {
			this.engine = engine;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getDatabase() {
			return database;
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setDatabase(String database) {
			this.database = database;
		}
		
		public String getSchema() {
			return schema;
		}

		public void setSchema(String schema) {
			this.schema = schema;
		}

		public boolean isRest() {
			return rest;
		}

		public void setRest(boolean rest) {
			this.rest = rest;
		}
	}
	
	
	public static class Auth {
		
		AuthType type;
		
		String ldapServerUrl;
		String[] userDnPatterns;
		String userSearchFilter;
		String userSearchBase;
		String groupSearchBase;
		String groupSearchFilter;
		
		String dbname;
		String dbDefaultUsername;
		String dbDefaultPassword;
		List<String> dbDefaultRoles = new ArrayList<>();
		boolean withGroups;
		
		String consumerKey;
		String consumerSecret;

		public String getLdapServerUrl() {
			return ldapServerUrl;
		}

		public void setLdapServerUrl(String ldapServer) {
			this.ldapServerUrl = ldapServer;
		}

		public String[] getUserDnPatterns() {
			return userDnPatterns;
		}

		public void setUserDnPatterns(String[] userDnPatterns) {
			this.userDnPatterns = userDnPatterns;
		}

		public String getUserSearchFilter() {
			return userSearchFilter;
		}

		public void setUserSearchFilter(String userSearchFilter) {
			this.userSearchFilter = userSearchFilter;
		}

		public String getUserSearchBase() {
			return userSearchBase;
		}

		public void setUserSearchBase(String userSearchBase) {
			this.userSearchBase = userSearchBase;
		}

		public String getGroupSearchBase() {
			return groupSearchBase;
		}


		public void setGroupSearchBase(String groupSearchBase) {
			this.groupSearchBase = groupSearchBase;
		}
		
		public String getGroupSearchFilter() {
			return groupSearchFilter;
		}

		public void setGroupSearchFilter(String groupSearchFilter) {
			this.groupSearchFilter = groupSearchFilter;
		}

		public String getDbname() {
			return dbname;
		}

		public void setDbname(String dbname) {
			this.dbname = dbname;
		}

		public AuthType getType() {
			return type;
		}

		public void setType(AuthType authType) {
			this.type = authType;
		}

		public String getConsumerKey() {
			return consumerKey;
		}

		public void setConsumerKey(String consumerKey) {
			this.consumerKey = consumerKey;
		}

		public String getConsumerSecret() {
			return consumerSecret;
		}

		public void setConsumerSecret(String consumerSecret) {
			this.consumerSecret = consumerSecret;
		}

		public String getDbDefaultUsername() {
			return dbDefaultUsername;
		}

		public void setDbDefaultUsername(String dbDefaultUsername) {
			this.dbDefaultUsername = dbDefaultUsername;
		}

		public String getDbDefaultPassword() {
			return dbDefaultPassword;
		}

		public void setDbDefaultPassword(String dbDefaultPassword) {
			this.dbDefaultPassword = dbDefaultPassword;
		}

		public List<String> getDbDefaultRoles() {
			return dbDefaultRoles;
		}

		public void setDbDefaultRoles(List<String> dbDefaultRoles) {
			this.dbDefaultRoles = dbDefaultRoles;
		}

		public boolean isWithGroups() {
			return withGroups;
		}

		public void setWithGroups(boolean withGroups) {
			this.withGroups = withGroups;
		}
	}
	
	public static enum AuthType {
		DB, LDAP, FACEBOOK, TWITTER, GOOGLE, FACEBOOK_CLIENT, GOOGLE_CLIENT
	}
	
	public static enum DBEngine {
		MYSQL, PGSQL, ORACLE, DB2, INFORMIX, HSQLDB, H2, MONGODB
	}
}
