package be.solidx.hot.data.jdbc.groovy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryBuilder;

public class JoinableCollection extends Collection implements be.solidx.hot.data.jdbc.JoinableCollection<Map<String, Object>> {
	
	protected Map<String, TableMetadata> tableMetadataMap;
	
	public JoinableCollection(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
			QueryBuilder queryBuilder, 
			String name, 
			Map<String, TableMetadata> tableMetadataMap) {
		super(namedParameterJdbcTemplate, queryBuilder, tableMetadataMap.get(name));
		this.tableMetadataMap = tableMetadataMap;
	}

	public Collection join (List<String> joinPaths) {
		JoinTree resultSetEntityTree = new JoinTree(tableMetadata.getName(), joinPaths, tableMetadataMap);
		Collection basicCollection = new Collection(namedParameterJdbcTemplate, queryBuilder, tableMetadata, resultSetEntityTree);
		return basicCollection;
	}

	@Override
	protected Map<String, Object> buildMap() {
		return new LinkedHashMap<String, Object>();
	}
}
