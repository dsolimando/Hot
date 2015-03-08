package be.solidx.hot.shows;

import java.util.List;

import be.solidx.hot.shows.Rest.RestAuthHeaders;

public interface RestConfig<CLOSURE,MAP> {

	RestAuthHeaders<CLOSURE> get(List<String> paths, MAP options);
	RestAuthHeaders<CLOSURE> get(String path, MAP options);
}
