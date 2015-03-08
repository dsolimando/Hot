package be.solidx.hot.promises;

public interface Deferred<CLOSURE> extends Promise<CLOSURE> {

	Deferred<CLOSURE> resolve	(Object... object);
	Deferred<CLOSURE> reject		(Object... object);
	Deferred<CLOSURE> notify		(Object... object);
	Promise<CLOSURE> promise	();
	
	
}
