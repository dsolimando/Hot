package be.icode.hot.data.jdbc.js;

import javax.sql.DataSource;

import org.mozilla.javascript.NativeObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractDB;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

public class DB extends AbstractDB<NativeObject> {

	public DB(QueryBuilder queryBuilder, DataSource dataSource, String schema) {
		super(queryBuilder, dataSource, schema);
	}
	
	@Override
	public JoinableCollection getCollection(String name) {
		return new JoinableCollection(new NamedParameterJdbcTemplate(dataSource), queryBuilder, name, tableMetadataMap);
	}
}
