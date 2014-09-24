package be.icode.hot.shows.groovy;

import java.util.concurrent.ExecutorService;

import be.icode.hot.Closure;
import be.icode.hot.groovy.GroovyClosure;
import be.icode.hot.shows.AbstractRest;

public class GroovyRest extends AbstractRest<groovy.lang.Closure<?>> {

	public GroovyRest(ExecutorService eventLoop) {
		super(eventLoop);
	}

	@Override
	protected Closure buildShowClosure(groovy.lang.Closure<?> closure) {
		return new GroovyClosure(closure);
	}
}
