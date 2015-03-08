package be.solidx.hot;

public interface ClosureExecutor<CLOSURE> {

	Object executeClosure(CLOSURE closure, Object... args);
}
