package be.solidx.hot.shows;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
		RestHeaders<CLOSURE> anonymous ();
		RestAuth<CLOSURE> headers (String[] headers);
		RestAuth<CLOSURE> headers (String header);
	}
	
	public interface RestAuth<CLOSURE> extends RestClosure<CLOSURE> {
		RestClosure<CLOSURE> auth (String... roles);
		RestClosure<CLOSURE> anonymous ();
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
