package be.solidx.hot.js;

import java.io.IOException;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class RhinoJsLoader {

	private String rootPath;
	
	private Context context;

	private Scriptable scope;

	public RhinoJsLoader(String rootPath, Context context, Scriptable scope) {
		this.rootPath = rootPath;
		this.context = context;
		this.scope = scope;
	}
	
	public RhinoJsLoader(Context context, Scriptable scope) {
		this.rootPath = "";
		this.context = context;
		this.scope = scope;
	}
	
	public void load (String path) throws IOException {
		context.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream(rootPath+"/"+path)), path, 1, null);
	}
}
