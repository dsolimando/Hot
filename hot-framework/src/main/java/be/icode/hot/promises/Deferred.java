package be.icode.hot.promises;

public interface Deferred<CLOSURE> extends Promise<CLOSURE> {

	void resolve	(Object... object);
	void reject		(Object... object);
	void notify		(Object... object);
	Promise<CLOSURE> promise	();
	
	
}
