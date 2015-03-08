package be.solidx.hot.web.deprecated;

import java.io.IOException;
import java.io.Writer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;

public interface HotController {

	ResponseEntity<String> handleScript(WebRequest webRequest, @PathVariable String scriptName);

	String handleScriptPOST(WebRequest webRequest, @PathVariable String scriptName, Writer writer) throws Exception;

	ResponseEntity<String> printHotPage(WebRequest webRequest, @PathVariable String page) throws IOException;

}