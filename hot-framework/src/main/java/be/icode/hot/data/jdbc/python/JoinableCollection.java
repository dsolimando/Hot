package be.icode.hot.data.jdbc.python;

import java.util.List;
import java.util.Map;

import org.python.core.PyDictionary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

public class JoinableCollection extends Collection implements be.icode.hot.data.jdbc.JoinableCollection<PyDictionary> {
	
	private Map<String, TableMetadata> tableMetadataMap;

	public JoinableCollection(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			String name, 
			Map<String, TableMetadata> tableMetadataMap) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadataMap.get(name));
		this.tableMetadataMap = tableMetadataMap;
	}
	
	@Override
	public Collection join(List<String> joinPaths) {
		JoinTree joinTree = new JoinTree(tableMetadata.getName(), joinPaths, tableMetadataMap);
		Collection collection = new Collection(namedParameterJdbcTemplate, queryBuilder, tableMetadata, joinTree);
		return collection;
	}
}
