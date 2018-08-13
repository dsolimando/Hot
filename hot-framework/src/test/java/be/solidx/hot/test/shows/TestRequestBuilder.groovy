package be.solidx.hot.test.shows

import be.solidx.hot.groovy.GroovyClosure
import be.solidx.hot.rest.HttpRequest
import be.solidx.hot.shows.ClosureRequestMapping
import be.solidx.hot.shows.RestRequest
import be.solidx.hot.shows.RestRequestBuilderFactory
import be.solidx.hot.spring.config.ShowConfig
import groovy.transform.CompileStatic
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.ldap.userdetails.LdapUserDetails
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionData
import org.springframework.social.security.SocialAuthenticationToken
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.support.AnnotationConfigContextLoader

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest

/**
 * Created by dsolimando on 12/08/2018.
 *
 * Copyright (C) 2010 - 2018 Solidx

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
@CompileStatic
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(
        loader = AnnotationConfigContextLoader ,
        classes = ShowConfig.class)
class TestRequestBuilder {

    @Inject
    RestRequestBuilderFactory requestBuilderFactory;

    @Test
    void testAuthenticateUsernamePassowrd() {
        ClosureRequestMapping closureRequestMapping = new ClosureRequestMapping(closure: new GroovyClosure({}))
        HttpServletRequest servletRequest = new HttpRequest(new URL('http://localhost/hot'),"application/json","GET",[:],'/rest')
        Authentication a = new UsernamePasswordAuthenticationToken("Damien","tttttt",[new SimpleGrantedAuthority("role1")])
        RestRequest request = requestBuilderFactory
                .build(closureRequestMapping)
                .newRestRequest(servletRequest)
                .authenticate(a)
                .build()

        assert request.user == [
                name: 'Damien',
                username: 'Damien',
                password: 'tttttt',
                roles: ['role1']
        ]
    }

    @Test
    void testAuthenticateUsernamePassowrdUser() {
        ClosureRequestMapping closureRequestMapping = new ClosureRequestMapping(closure: new GroovyClosure({}))
        HttpServletRequest servletRequest = new HttpRequest(new URL('http://localhost/hot'),"application/json","GET",[:],'/rest')
        Authentication a = Mockito.mock(UsernamePasswordAuthenticationToken)

        Mockito.when(a.getPrincipal()).thenReturn(new User('Damien','tttttt',[new SimpleGrantedAuthority("role1")]))

        RestRequest request = requestBuilderFactory
                .build(closureRequestMapping)
                .newRestRequest(servletRequest)
                .authenticate(a)
                .build()

        assert request.user == [
                name: 'Damien',
                username: 'Damien',
                password: 'tttttt',
                roles: ['role1']
        ]
    }

    @Test
    void testAuthenticateUsernamePassowrdLDAP() {
        ClosureRequestMapping closureRequestMapping = new ClosureRequestMapping(closure: new GroovyClosure({}))
        HttpServletRequest servletRequest = new HttpRequest(new URL('http://localhost/hot'),"application/json","GET",[:],'/rest')

        Authentication a = Mockito.mock(UsernamePasswordAuthenticationToken.class)

        LdapUserDetails userDetails = Mockito.mock(LdapUserDetails.class)
        Mockito.when(userDetails.getUsername()).thenReturn("slmdmn")
        Mockito.when(userDetails.getPassword()).thenReturn("tttttt")
        Mockito.when(userDetails.getDn()).thenReturn("mydn")

        GrantedAuthority authority = Mockito.mock(GrantedAuthority.class)
        Mockito.when(authority.getAuthority()).thenReturn("role1")
        Mockito.when(userDetails.getAuthorities()).thenReturn([authority])

        Mockito.when(a.getPrincipal()).thenReturn(userDetails)

        RestRequest request = requestBuilderFactory
                .build(closureRequestMapping)
                .newRestRequest(servletRequest)
                .authenticate(a)
                .build()

        assert request.user == [
                name: 'slmdmn',
                username: 'slmdmn',
                password: 'tttttt',
                dn: 'mydn',
                roles: ['role1']
        ]
    }

    @Test
    void testAuthenticateSocial() {
        ClosureRequestMapping closureRequestMapping = new ClosureRequestMapping(closure: new GroovyClosure({}))
        HttpServletRequest servletRequest = new HttpRequest(new URL('http://localhost/hot'),"application/json","GET",[:],'/rest')

        SocialAuthenticationToken a = Mockito.mock(SocialAuthenticationToken.class)
        Mockito.when(a.getName()).thenReturn('dsolimando')

        ConnectionData cd = Mockito.mock(ConnectionData.class)
        Mockito.when(cd.getAccessToken()).thenReturn("123")
        Mockito.when(cd.getImageUrl()).thenReturn("http://localhost/image.jpg")
        Mockito.when(cd.getProfileUrl()).thenReturn("http://localhost/profile")
        Mockito.when(cd.getProviderUserId()).thenReturn("321")
        Mockito.when(cd.getProviderId()).thenReturn("twitter")
        Mockito.when(cd.getExpireTime()).thenReturn(33l)

        def connection = Mockito.mock(Connection.class)
        Mockito.when(connection.createData()).thenReturn(cd)
        Mockito.when(a.getConnection()).thenReturn(connection)

        RestRequest request = requestBuilderFactory
                .build(closureRequestMapping)
                .newRestRequest(servletRequest)
                .authenticate(a)
                .build()

        assert request.user == [
                name: 'dsolimando',
                username: 'dsolimando',
                accessToken: '123',
                picture: 'http://localhost/image.jpg',
                link: 'http://localhost/profile',
                id: '321',
                provider: 'twitter',
                expiresIn: 33l
        ]
    }

    @Test
    void testWithObjectBody() {
        ClosureRequestMapping closureRequestMapping = new ClosureRequestMapping(closure: new GroovyClosure({}))
        HttpServletRequest servletRequest = new HttpRequest(new URL('http://localhost/hot'),"application/json","GET",[:],'/rest')

        RestRequest request = requestBuilderFactory
                .build(closureRequestMapping)
                .newRestRequest(servletRequest)
                .withBody([name:'Damien'])
                .build()

        assert request.body == [name:'Damien']
    }

    @Test
    void testWithBodyToSerialize() {

        ClosureRequestMapping closureRequestMapping = new ClosureRequestMapping(closure: new GroovyClosure({}))
        Map<String, Enumeration<String>> headers = new HashMap<String, Enumeration<String>>();
        headers.put("Content-Type",new Vector<>(Arrays.asList("application/json")).elements());
        HttpServletRequest servletRequest = new HttpRequest(new URL('http://localhost/hot'),"application/json","GET",headers,'/rest')

        RestRequest request = requestBuilderFactory
                .build(closureRequestMapping)
                .newRestRequest(servletRequest)
                .withBodyConversion("""{"name":"Damien"}""".bytes)
                .build()
        assert request.body == [name:'Damien']
    }
}
