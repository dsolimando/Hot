package be.solidx.hot.groovy;

import be.solidx.hot.Closure;

public class GroovyClosure implements Closure {

	groovy.lang.Closure<?> closure;
	
	public GroovyClosure(groovy.lang.Closure<?> closure) {
		this.closure = closure; 
	}

	@Override
	public Object call(Object... objects) {
		return closure.call(objects); 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof GroovyClosure) {
			GroovyClosure groovyClosure = (GroovyClosure) obj;
			return closure.getMetaClass().hashCode() == groovyClosure.closure.getMetaClass().hashCode();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return closure.getMetaClass().hashCode();
	}
}
