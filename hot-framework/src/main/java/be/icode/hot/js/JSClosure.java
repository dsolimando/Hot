package be.icode.hot.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;

import be.icode.hot.Closure;

public class JSClosure implements Closure {

	protected NativeFunction nativeFunction;
	protected Scriptable globalscope;
	
	public JSClosure(NativeFunction nativeFunction, Scriptable globalscope) {
		this.nativeFunction = nativeFunction;
		this.globalscope = globalscope;
	}

	@Override
	public Object call(Object...objects) {
		Context context = Context.enter();
		Object object = nativeFunction.call(context,globalscope,nativeFunction, objects);
		Context.exit();
		return object;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof JSClosure) {
			JSClosure jsClosure = (JSClosure) obj;
			return jsClosure.nativeFunction.getEncodedSource().hashCode() == nativeFunction.getEncodedSource().hashCode();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return nativeFunction.getEncodedSource().hashCode();
	}
}
