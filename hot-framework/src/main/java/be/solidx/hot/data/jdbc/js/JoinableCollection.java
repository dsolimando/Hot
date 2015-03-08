package be.solidx.hot.data.jdbc.js;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class JoinableCollection extends Collection implements be.solidx.hot.data.jdbc.JoinableCollection<NativeObject>{

	protected Map<String, TableMetadata> tableMetadataMap;
	
	public JoinableCollection(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, String name,
			Map<String, TableMetadata> tableMetadataMap) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadataMap.get(name));
		this.tableMetadataMap = tableMetadataMap;
	}

	public Collection join(List<String> joinPaths) {
		JoinTree resultSetEntityTree = new JoinTree(tableMetadata.getName(), joinPaths, tableMetadataMap);
		Collection collection = new Collection(namedParameterJdbcTemplate, queryBuilder, tableMetadata, resultSetEntityTree);
		return collection;
	}
}
