package be.solidx.hot.data.jdbc;

import java.util.List;
import java.util.Map;

import be.solidx.hot.data.Collection;

public interface JoinableCollection<T extends Map<?,?>> extends Collection<T> {
	Collection<T> join(List<String> joinPaths);
}
