package be.icode.hot.shows;

import java.util.List;

public interface Rest<CLOSURE> {
	
	List<ClosureRequestMapping> getRequestMappings();
	
	RestAuthHeaders<CLOSURE> put(List<String> paths);
	RestAuthHeaders<CLOSURE> get(List<String> paths);
	RestAuthHeaders<CLOSURE> get(String path);
	RestAuthHeaders<CLOSURE> post(List<String> paths);
	RestAuthHeaders<CLOSURE> delete(List<String> paths);
	RestAuthHeaders<CLOSURE> put(String path);
	RestAuthHeaders<CLOSURE> post(String path);
	RestAuthHeaders<CLOSURE> delete(String path);

	public interface RestAuthHeaders<CLOSURE> extends RestClosure<CLOSURE> {
		RestHeaders<CLOSURE> auth (String...roles);
		RestAuth<CLOSURE> headers (String[] headers);
		RestAuth<CLOSURE> headers (String header);
	}
	
	public interface RestAuth<CLOSURE> extends RestClosure<CLOSURE> {
		RestClosure<CLOSURE> auth (String... roles);
	}
	
	public interface RestHeaders<CLOSURE> extends RestClosure<CLOSURE> {
		RestClosure<CLOSURE> headers(String[] headers);
		RestClosure<CLOSURE> headers (String header);
	}
	
	public interface RestClosure<CLOSURE> {
		void then (CLOSURE closure);
		void now(CLOSURE closure);
	}
}
