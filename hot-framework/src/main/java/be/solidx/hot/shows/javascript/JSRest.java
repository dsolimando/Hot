package be.solidx.hot.shows.javascript;

import java.util.concurrent.ExecutorService;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.solidx.hot.Closure;
import be.solidx.hot.js.JSClosure;
import be.solidx.hot.shows.AbstractRest;

public class JSRest extends AbstractRest<NativeFunction> {
	
	Scriptable globalScope;
	
	public JSRest(ExecutorService eventLoop, Scriptable globalScope) {
		super(eventLoop);
		this.globalScope = globalScope;
	}

	@Override
	protected Closure buildShowClosure(NativeFunction closure) {
		return new JSClosure(closure,globalScope);
	}
}
