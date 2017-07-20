package be.solidx.hot.test.shows;

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
