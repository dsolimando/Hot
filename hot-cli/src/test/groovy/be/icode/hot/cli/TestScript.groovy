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
package be.icode.hot.cli

import be.solidx.hot.cli.Hot
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.Before
import org.junit.Test

class TestScript {

    static ObjectMapper objectMapper = new ObjectMapper()

    static {
        objectMapper.getSerializationConfig().with(MapperFeature.USE_STATIC_TYPING)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        objectMapper.getSerializationConfig().with(MapperFeature.USE_ANNOTATIONS)
        objectMapper.configOverride(Map.class).setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
    }

    @Before
    void before() {
        new File("/tmp/ptest").deleteDir()
    }

    @Test
    void projectCreationTest() {

        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest"].toArray())

        assert new File("/tmp/ptest").exists()
        assert new File("/tmp/ptest/config.json").exists()
        assert new File("/tmp/ptest/www").exists()
        assert new File("/tmp/ptest/shows").exists()
        assert new File("/tmp/ptest/shows/example.show.groovy").exists()
        assert new File("/tmp/ptest/shows/example.show.js").exists()
        assert new File("/tmp/ptest/shows/example.show.py").exists()
        assert new File("/tmp/ptest/www/index.html").exists()
        assert new File("/tmp/ptest/.work").exists()
        assert new File("/tmp/ptest/.work/resources").exists()
        assert new File("/tmp/ptest/.work/sql").exists()

        assert ["name"   : "ptest",
                "nature" : "jee",
                "version": "0.1",
                "devMode": true] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void projectCreationTestCurrentDir() {

        Hot hot = new Hot("", "ptest")
        hot.handleArgs(["create", "-n", "ptest"].toArray())

        assert new File("ptest").exists()
        assert new File("ptest/config.json").exists()
        assert new File("ptest/www").exists()
        assert new File("ptest/shows").exists()
        assert new File("ptest/shows/example.show.groovy").exists()
        assert new File("ptest/shows/example.show.js").exists()
        assert new File("ptest/shows/example.show.py").exists()
        assert new File("ptest/www/index.html").exists()
        assert new File("ptest/.work").exists()
        assert new File("ptest/.work/resources").exists()
        assert new File("ptest/.work/sql").exists()

        assert ["name"   : "ptest",
                "nature" : "jee",
                "version": "0.1",
                "devMode": true] == objectMapper.readValue(new File("ptest/config.json"), Map.class)

        new File("ptest").deleteDir()
    }

    @Test
    void projectCreationTestVersion() {

        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())

        assert ["name"   : "ptest",
                "nature" : "jee",
                "version": "0.2",
                "devMode": true] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void testUpdateProject() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["update", "-n", "ptest2", "-v", "0.3"].toArray())

        assert ["name"   : "ptest2",
                "nature" : "jee",
                "version": "0.3",
                "devMode": true] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void addMysqlDataSource() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["mysql",
                        "-n", "myds",
                        "-h", "192.168.0.2",
                        "-port", "1111",
                        "-db", "petclinic",
                        "-u", "mysqlu",
                        "-p", "mysqlp"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "myds",
                                "engine"  : "MYSQL",
                                "hostname": "192.168.0.2",
                                "port"    : 1111,
                                "database": "petclinic",
                                "username": "mysqlu",
                                "password": "mysqlp"
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addMysqlDataSourceDefaults() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["mysql",
                        "-n", "myds",
                        "-db", "petclinic",
                        "-u", "root"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "myds",
                                "engine"  : "MYSQL",
                                "hostname": "localhost",
                                "port"    : 3306,
                                "database": "petclinic",
                                "username": "root",
                                "password": ""
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addhsqldbDataSource() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["hsqldb",
                        "-n", "h2ds",
                        "-db", "myhsqldb",
                        "-s", "petclinic",
                        "-u", "sap",
                        "-p", "vide"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "h2ds",
                                "engine"  : "HSQLDB",
                                "database": "myhsqldb",
                                "schema"  : "petclinic",
                                "username": "sap",
                                "password": "vide"
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addhsqldbDataSourceDefaults() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["hsqldb",
                        "-n", "h2ds",
                        "-db", "myhsqldb",
                        "-s", "petclinic",
                        "-u", "sap"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "h2ds",
                                "engine"  : "HSQLDB",
                                "database": "myhsqldb",
                                "schema"  : "petclinic",
                                "username": "sap",
                                "password": ""
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addOracleDataSource() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["oracle",
                        "-n", "orads",
                        "-h", "192.168.0.3",
                        "-port", "1522",
                        "-sid", "XA",
                        "-s", "petclinic",
                        "-u", "oracleu",
                        "-p", "oraclep"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "orads",
                                "engine"  : "ORACLE",
                                "hostname": "192.168.0.3",
                                "port"    : 1522,
                                "service" : "XA",
                                "schema"  : "petclinic",
                                "username": "oracleu",
                                "password": "oraclep"
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addOracleDataSourceDefaults() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["oracle",
                        "-n", "orads",
                        "-sid", "XA",
                        "-s", "petclinic",
                        "-u", "oracleu"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "orads",
                                "engine"  : "ORACLE",
                                "hostname": "localhost",
                                "port"    : 1521,
                                "service" : "XA",
                                "schema"  : "petclinic",
                                "username": "oracleu",
                                "password": ""
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addPgsqlDataSource() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["pgsql",
                        "-n", "pgds",
                        "-h", "192.168.0.4",
                        "-port", "5433",
                        "-db", "mypgdatabase",
                        "-s", "petclinic",
                        "-u", "pgu",
                        "-p", "pgp"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "pgds",
                                "engine"  : "PGSQL",
                                "hostname": "192.168.0.4",
                                "port"    : 5433,
                                "database": "mypgdatabase",
                                "schema"  : "petclinic",
                                "username": "pgu",
                                "password": "pgp"
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addPgsqlDataSourceDefaults() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["pgsql",
                        "-n", "pgds",
                        "-db", "mypgdatabase",
                        "-s", "petclinic",
                        "-u", "pgu"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "pgds",
                                "engine"  : "PGSQL",
                                "hostname": "localhost",
                                "port"    : 5432,
                                "database": "mypgdatabase",
                                "schema"  : "petclinic",
                                "username": "pgu",
                                "password": ""
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addDb2DataSource() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["db2",
                        "-n", "db2ds",
                        "-h", "192.168.0.5",
                        "-port", "50001",
                        "-db", "db2database",
                        "-s", "petclinic",
                        "-u", "db2u",
                        "-p", "db2p"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "db2ds",
                                "engine"  : "DB2",
                                "hostname": "192.168.0.5",
                                "port"    : 50001,
                                "database": "db2database",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": "db2p"
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addDb2DataSourceDefaults() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["db2",
                        "-n", "db2ds",
                        "-db", "db2database",
                        "-s", "petclinic",
                        "-u", "db2u"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "db2ds",
                                "engine"  : "DB2",
                                "hostname": "localhost",
                                "port"    : 50000,
                                "database": "db2database",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addMongoDataSource() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.3"].toArray())
        hot.handleArgs(["mongo",
                        "-n", "mongods",
                        "-h", "192.168.0.15",
                        "-port", "27018",
                        "-db", "mongodatabase",
                        "-u", "mongou",
                        "-p", "mongop"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.3",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "mongods",
                                "engine"  : "MONGODB",
                                "hostname": "192.168.0.15",
                                "port"    : 27018,
                                "database": "mongodatabase",
                                "username": "mongou",
                                "password": "mongop"
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void addMongoDataSourceDefaults() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.3"].toArray())
        hot.handleArgs(["mongo",
                        "-n", "mongods", "-db", "mongodatabase"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.3",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "mongods",
                                "engine"  : "MONGODB",
                                "hostname": "localhost",
                                "port"    : 27017,
                                "database": "mongodatabase",
                                "username": "",
                                "password": ""
                            ]]] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void testRemoveDS() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["db2",
                        "-n", "db2ds",
                        "-db", "db2database",
                        "-s", "petclinic",
                        "-u", "db2u"].toArray())
        hot.handleArgs(["pgsql",
                        "-n", "pgds",
                        "-db", "mypgdatabase",
                        "-s", "petclinic",
                        "-u", "pgu"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "db2ds",
                                "engine"  : "DB2",
                                "hostname": "localhost",
                                "port"    : 50000,
                                "database": "db2database",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ], [
                                "name"    : "pgds",
                                "engine"  : "PGSQL",
                                "hostname": "localhost",
                                "port"    : 5432,
                                "database": "mypgdatabase",
                                "schema"  : "petclinic",
                                "username": "pgu",
                                "password": ""
                            ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["rmdb", "-n", "pgds"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "db2ds",
                                "engine"  : "DB2",
                                "hostname": "localhost",
                                "port"    : 50000,
                                "database": "db2database",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void testRestInterfaces() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["db2",
                        "-n", "db2ds",
                        "-db", "db2database",
                        "-s", "petclinic",
                        "-u", "db2u"].toArray())
        hot.handleArgs(["pgsql",
                        "-n", "pgds",
                        "-db", "mypgdatabase",
                        "-s", "petclinic",
                        "-u", "pgu"].toArray())
        hot.handleArgs(["restds", "-n", "pgds"].toArray())

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.2",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "db2ds",
                                "engine"  : "DB2",
                                "hostname": "localhost",
                                "port"    : 50000,
                                "database": "db2database",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ], [
                                "name"    : "pgds",
                                "engine"  : "PGSQL",
                                "hostname": "localhost",
                                "port"    : 5432,
                                "database": "mypgdatabase",
                                "schema"  : "petclinic",
                                "username": "pgu",
                                "password": "",
                                "rest"    : true
                            ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void testAuthDB() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())
        hot.handleArgs(["oracle",
                        "-n", "oracleds",
                        "-service", "pets",
                        "-s", "petclinic",
                        "-u", "db2u"].toArray())

        hot.handleArgs(["auth-db", "-n", "oracleds", "-u", "hot", "-p", "hot", "-roles", "ADMIN,USER"].toArray())

        println objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.1",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "oracleds",
                                "engine"  : "ORACLE",
                                "hostname": "localhost",
                                "port"    : 1521,
                                "service" : "pets",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ]],
            "authList"   : [[
                                "type"             : "DB",
                                "dbname"           : "oracleds",
                                "dbDefaultUsername": "hot",
                                "dbDefaultPassword": "hot",
                                "dbDefaultRoles"   : ["ADMIN", "USER"]
                            ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-db", "-n", "oracleds"].toArray())
        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.1",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "oracleds",
                                "engine"  : "ORACLE",
                                "hostname": "localhost",
                                "port"    : 1521,
                                "service" : "pets",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ]],
            "authList"   : [[
                                "type"  : "DB",
                                "dbname": "oracleds"
                            ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        assert new File("/tmp/ptest/sql/oracle-auth-init.sql").exists()

        hot.handleArgs(["auth-db", "-r"].toArray())
        assert [
            "name"       : "ptest",
            "nature"     : "jee",
            "version"    : "0.1",
            "devMode"    : true,
            "dataSources": [[
                                "name"    : "oracleds",
                                "engine"  : "ORACLE",
                                "hostname": "localhost",
                                "port"    : 1521,
                                "service" : "pets",
                                "schema"  : "petclinic",
                                "username": "db2u",
                                "password": ""
                            ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void testAuthLDAP() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())

        hot.handleArgs(["auth-ldap",
                        "-url", "ldap://springframework.org:389/dc=springframework,dc=org",
                        "-usb", "ou=people",
                        "-usf", "uid={0}",
                        "-gsb", "ou=groups",
                        "-gsf", "(uniqueMember={0})"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"             : "LDAP",
                             "ldapServerUrl"    : "ldap://springframework.org:389/dc=springframework,dc=org",
                             "userSearchBase"   : "ou=people",
                             "userSearchFilter" : "uid={0}",
                             "groupSearchBase"  : "ou=groups",
                             "groupSearchFilter": "(uniqueMember={0})"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-ldap",
                        "-url", "ldap://springframework.org:389/dc=springframework,dc=org",
                        "-udp", "uid={0},ou=people",
                        "-gsb", "ou=groups",
                        "-gsf", "(uniqueMember={0})"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"             : "LDAP",
                             "ldapServerUrl"    : "ldap://springframework.org:389/dc=springframework,dc=org",
                             "userDnPatterns"   : ["uid={0}", "ou=people"],
                             "groupSearchBase"  : "ou=groups",
                             "groupSearchFilter": "(uniqueMember={0})"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)


        hot.handleArgs(["auth-ldap",
                        "-url", "ldap://springframework.org:389/dc=springframework,dc=org",
                        "-udp", "uid={0},ou=people"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "LDAP",
                             "ldapServerUrl" : "ldap://springframework.org:389/dc=springframework,dc=org",
                             "userDnPatterns": ["uid={0}", "ou=people"],
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-ldap",
                        "-url", "ldap://springframework.org:389/dc=springframework,dc=org",
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"         : "LDAP",
                             "ldapServerUrl": "ldap://springframework.org:389/dc=springframework,dc=org",
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-ldap", "-r"].toArray())
        assert [
            "name"   : "ptest",
            "nature" : "jee",
            "version": "0.1",
            "devMode": true
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void testJWT_JWKS() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())
        hot.handleArgs(["jwt", 'add',
                        "-n", "apple",
                        "-aud", "scell",
                        "-c", "email,is_private_email",
                        "-a", "RS256",
                        "-url", "https://appleid.apple.com/auth/keys",
        ].toArray())
        assert [
            'name': 'ptest',
            'nature': 'jee',
            'version': '0.1',
            'devMode': true,
            'authList':
                [
                    [
                        'type': 'JWT',
                        'name': 'apple',
                        'audience': 'scell',
                        'claims': 'email,is_private_email',
                        'algorithm': 'RS256',
                        'jwksUrl': 'https://appleid.apple.com/auth/keys']
                ]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["jwt", "rm", '-n', 'apple'].toArray())
        assert [
            "name"   : "ptest",
            "nature" : "jee",
            "version": "0.1",
            "devMode": true
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void testJWT_HMAC() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())
        hot.handleArgs(["jwt", 'add',
                        "-n", "apple",
                        "-aud", "scell",
                        "-c", "email,is_private_email",
                        "-a", "RS256",
                        "-s", "01234567890",
        ].toArray())
        assert [
            'name': 'ptest',
            'nature': 'jee',
            'version': '0.1',
            'devMode': true,
            'authList':
                [
                    [
                        'type': 'JWT',
                        'name': 'apple',
                        'audience': 'scell',
                        'claims': 'email,is_private_email',
                        'algorithm': 'RS256',
                        'secret': '01234567890']
                ]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)
    }

    @Test
    void testJWT_Fail() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())
        hot.handleArgs(["jwt", 'add',
                        "-n", "apple",
                        "-aud", "scell",
                        "-c", "email,is_private_email",
                        "-a", "RS256",
        ].toArray())
        assert [
            'name'   : 'ptest',
            'nature' : 'jee',
            'version': '0.1',
            'devMode': true,
        ]
    }

    @Test
    void testAuthFacebook() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())

        hot.handleArgs(["auth-facebook",
                        "-id", "azerty",
                        "-sec", "qsdfgh"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "FACEBOOK",
                             "consumerKey"   : "azerty",
                             "consumerSecret": "qsdfgh"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-facebook",
                        "-id", "qsdfgh",
                        "-sec", "azerty"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "FACEBOOK",
                             "consumerKey"   : "qsdfgh",
                             "consumerSecret": "azerty"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-facebook", "-r"].toArray())
        assert [
            "name"   : "ptest",
            "nature" : "jee",
            "version": "0.1",
            "devMode": true
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void testAuthTwitter() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())

        hot.handleArgs(["auth-twitter",
                        "-ck", "azerty",
                        "-cp", "qsdfgh"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "TWITTER",
                             "consumerKey"   : "azerty",
                             "consumerSecret": "qsdfgh"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-twitter",
                        "-ck", "qsdfgh",
                        "-cp", "azerty"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "TWITTER",
                             "consumerKey"   : "qsdfgh",
                             "consumerSecret": "azerty"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-twitter", "-r"].toArray())
        assert [
            "name"   : "ptest",
            "nature" : "jee",
            "version": "0.1",
            "devMode": true
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void testAuthGoogle() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.1"].toArray())

        hot.handleArgs(["auth-google",
                        "-id", "azerty",
                        "-sec", "qsdfgh"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "GOOGLE",
                             "consumerKey"   : "azerty",
                             "consumerSecret": "qsdfgh"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-google",
                        "-id", "qsdfgh",
                        "-sec", "azerty"
        ].toArray())

        assert [
            "name"    : "ptest",
            "nature"  : "jee",
            "version" : "0.1",
            "devMode" : true,
            "authList": [[
                             "type"          : "GOOGLE",
                             "consumerKey"   : "qsdfgh",
                             "consumerSecret": "azerty"
                         ]]
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

        hot.handleArgs(["auth-google", "-r"].toArray())
        assert [
            "name"   : "ptest",
            "nature" : "jee",
            "version": "0.1",
            "devMode": true
        ] == objectMapper.readValue(new File("/tmp/ptest/config.json"), Map.class)

    }

    @Test
    void testPrepareWar() {
        Hot hot = new Hot("/tmp", "ptest")
        hot.handleArgs(["create", "-n", "ptest", "-v", "0.2"].toArray())
        hot.handleArgs(["db2",
                        "-n", "db2ds",
                        "-db", "db2database",
                        "-s", "petclinic",
                        "-u", "db2u"].toArray())
        hot.handleArgs(["pgsql",
                        "-n", "pgds",
                        "-db", "mypgdatabase",
                        "-s", "petclinic",
                        "-u", "pgu"].toArray())
        hot.handleArgs(["restds", "-n", "pgds"].toArray())
        def jsp = new File("/tmp/ptest/www/toto.jsp").createNewFile()
        hot.prepareWar new Hot.Project("ptest", "/tmp")

        assert new File("/tmp/ptest").exists()
        assert new File("/tmp/ptest/.build").exists()
        assert new File("/tmp/ptest/.build/WEB-INF/web.xml").exists()
        assert new File("/tmp/ptest/.build/classes").exists()
        assert !new File("/tmp/ptest/.build/classes/toto.jsp").exists()
        assert new File("/tmp/ptest/.build/classes/config.json").exists()
        assert new File("/tmp/ptest/.build/classes/logback.xml").exists()
        assert new File("/tmp/ptest/.build/classes/sql").exists()
        assert new File("/tmp/ptest/.build/classes/sql")
    }
}
