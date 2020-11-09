package be.solidx.hot.test.shows;

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

import be.solidx.hot.groovy.GroovyScriptExecutor;
import be.solidx.hot.js.JSScriptExecutor;
import be.solidx.hot.python.PythonScriptExecutor;
import be.solidx.hot.rest.HttpRequest;
import be.solidx.hot.shows.ClosureRequestMapping;
import be.solidx.hot.shows.ShowsContext;
import be.solidx.hot.shows.spring.ClosureRequestMappingHandlerMapping;
import be.solidx.hot.spring.config.ThreadPoolsConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Named;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by dsolimando on 13/07/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {TestScaling.Config.class})
public class TestScaling {

    @Autowired
    ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping;

    @Test
    public void testScaling () throws Exception {
        HttpRequest httpRequest = new HttpRequest(
                new URL(
                        "http://hot.solidx.be:8080/app/repository/scaling"),
                "application/json",
                "GET",
                new HashMap<String, Enumeration<String>>(),
                "/repository");

        boolean different = true;
        ClosureRequestMapping prevClosureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(httpRequest);;

        for (int i = 1; i < Runtime.getRuntime().availableProcessors(); i++) {
            ClosureRequestMapping closureRequestMapping = closureRequestMappingHandlerMapping.lookupRequestMapping(httpRequest);
            different &= closureRequestMapping != prevClosureRequestMapping;
            prevClosureRequestMapping = closureRequestMapping;
        }
        Assert.assertTrue(different);
    }

    @Configuration
    @Import({ThreadPoolsConfig.class})
    public static class Config {

        @Bean
        public GroovyScriptExecutor groovyScriptExecutor() {
            return new GroovyScriptExecutor();
        }

        @Bean
        public PythonScriptExecutor pythonScriptExecutor() {
            return new PythonScriptExecutor();
        }

        @Bean
        public JSScriptExecutor jsScriptExecutor() {
            return new JSScriptExecutor();
        }

        @Bean
        public ClosureRequestMappingHandlerMapping closureRequestMappingHandlerMapping(ShowsContext showsContext, ApplicationContext applicationContext) {
            ClosureRequestMappingHandlerMapping handlerMapping = new ClosureRequestMappingHandlerMapping(showsContext);
            handlerMapping.setApplicationContext(applicationContext);
            return handlerMapping;
        }

        @Bean
        public ShowsContext ShowsContext(
                ApplicationContext applicationContext,
                ThreadPoolsConfig.EventLoopFactory eventLoopFactory,
                @Named("blockingTasksThreadPool") ExecutorService blockingThreadPool,
                GroovyScriptExecutor groovyScriptExecutor,
                PythonScriptExecutor pythonScriptExecutor,
                JSScriptExecutor jsScriptExecutor) throws IOException {
            ShowsContext ctx = new ShowsContext(applicationContext,
                    eventLoopFactory,
                    blockingThreadPool,
                    groovyScriptExecutor,
                    jsScriptExecutor,
                    pythonScriptExecutor,
                    null, null, null, null, null,null,null,null,null,null);

            ctx.setDefaultShowSearchPath("/scaling");
            ctx.loadShows();
            return ctx;
        }
    }
}
