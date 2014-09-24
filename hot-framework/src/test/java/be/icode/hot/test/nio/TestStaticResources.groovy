package be.icode.hot.test.nio;

import static org.junit.Assert.*
import groovy.transform.CompileStatic

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import javax.servlet.http.HttpServletRequest

import org.junit.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult

import be.icode.hot.js.JSScriptExecutor
import be.icode.hot.js.transpilers.CoffeeScriptCompiler
import be.icode.hot.js.transpilers.LessCompiler
import be.icode.hot.rest.HttpRequest
import be.icode.hot.utils.FileLoader
import be.icode.hot.web.TranspiledScriptsController

@CompileStatic
class TestStaticResources {

	ExecutorService eventLoop = Executors.newSingleThreadExecutor()
	ExecutorService blockingTaskQueue = Executors.newCachedThreadPool()
	
	FileLoader fileLoader = new FileLoader(eventLoop)
	JSScriptExecutor jsScriptExecutor = new JSScriptExecutor()
	LessCompiler lessCompiler = new LessCompiler()
	CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler()
	
	TranspiledScriptsController controller = new TranspiledScriptsController(fileLoader, blockingTaskQueue, eventLoop , lessCompiler,coffeeScriptCompiler, false)
	
	@Test
	public void testConcat() {
		
		URL url = new URL("http://localhost/theapp/concat/be/icode/hot/test/mvc/f1.js,be/icode/hot/test/mvc/f2.js,be/icode/hot/test/mvc/f3.js,be/icode/hot/test/mvc/f4.js,be/icode/hot/test/mvc/f5.js")
		HttpServletRequest request = new HttpRequest(url, null, "GET", null, "")
		
		DeferredResult<?> result = controller.getJsResource(request)
		
		assert result != null
		while(!result.result) {
			//println "waiting"
			sleep 2000
		}
		ResponseEntity<byte[]> re = (ResponseEntity<byte[]>)result.result
		println new String(re.body)
		assert new String(re.body) == ''';function f1() {
	return true
}
;function f1() {
	return true
}
;function f2() {
	return false
}
;function f3() {
	return true
}
;function f4() {
	return true
}
;function f5() {
	return true
}'''
	}
	
	@Test
	void testLessResource() {
		jsScriptExecutor.setInterpretive(true);
		jsScriptExecutor.setGlobalScopeScripts(["/js/less.js"]);
		lessCompiler.jsScriptExecutor = jsScriptExecutor
		coffeeScriptCompiler.jsScriptExecutor = jsScriptExecutor
		
		URL url = new URL("http://localhost/theapp/be/icode/hot/test/mvc/style.less")
		HttpServletRequest request = new HttpRequest(url, null, "GET", null, "")
		
		DeferredResult<?> result = controller.getLESSResource("style",request)
		assert result != null
		while(!result.result) {
			//println "waiting"
			sleep 2000
		}
		ResponseEntity<byte[]> re = (ResponseEntity<byte[]>)result.result
		assert '''#header {
  color: black;
}
#header .navigation {
  font-size: 12px;
}
#header .logo {
  width: 300px;
}''' == new String(re.body).trim()

		DeferredResult<?> result2 = controller.getLESSResource("style2",request)
		assert result2 != null
		while(!result2.result) {
			//println "waiting"
			sleep 2000
		}
		ResponseEntity<byte[]> re2 = (ResponseEntity<byte[]>)result2.result
		assert '''#header {
  color: black;
}
#header .navigation {
  font-size: 12px;
}
#header .logo {
  width: 300px;
}''' == new String(re2.body).trim()
		println new String(re2.body).trim()
	}
	
	@Test
	void testCoffeeResource() {
		jsScriptExecutor.setInterpretive(true);
		jsScriptExecutor.setGlobalScopeScripts(["/js/coffee-script.js"]);
		lessCompiler.jsScriptExecutor = jsScriptExecutor
		coffeeScriptCompiler.jsScriptExecutor = jsScriptExecutor
		
		URL url = new URL("http://localhost/theapp/be/icode/hot/test/mvc/main.coffee")
		HttpServletRequest request = new HttpRequest(url, null, "GET", null, "")
		
		DeferredResult<?> result = controller.getCoffeeScriptResource("coffe",request)
		assert result != null
		while(!result.result) {
			//println "waiting"
			sleep 2000
		}
		ResponseEntity<byte[]> re = (ResponseEntity<byte[]>)result.result
		assert '''(function() {
  var Resources;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };
  Resources = (function() {
    function Resources() {
      this.bus = $("body");
    }
    Resources.prototype.stations = function(letter) {
      return $.ajax({
        url: "rest/stations/" + letter,
        dataType: "json",
        success: __bind(function(response) {
          var event;
          event = jQuery.Event("stationRetievalSuccess");
          event.response = response;
          event.letter = letter;
          return this.bus.trigger(event);
        }, this),
        error: __bind(function(a, b, c) {
          return this.bus.trigger("stationRetievalError");
        }, this)
      });
    };
    return Resources;
  })();
}).call(this);''' == new String(re.body).trim()
	}
}
