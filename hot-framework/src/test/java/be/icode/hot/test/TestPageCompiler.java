package be.icode.hot.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import be.icode.hot.Script;
import be.icode.hot.groovy.GroovyPageCompiler;
import be.icode.hot.utils.IOUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestPageCompiler {

	@Autowired
	GroovyPageCompiler groovyPageCompiler;
	
	@Test
	public void testCompile1() throws Exception {
		Script<String> page = new Script<String>(IOUtils.loadBytesNoCache("page1-content.hotg"), "page1-content.hotg");
		String s = groovyPageCompiler.compile(page);
		System.out.println(s);
		Assert.assertEquals(org.apache.commons.io.IOUtils.toString(IOUtils.loadResourceNoCache("page1.result")), s.trim());
	}
}
