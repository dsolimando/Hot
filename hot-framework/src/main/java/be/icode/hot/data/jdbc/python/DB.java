package be.icode.hot.data.jdbc.python;

import javax.sql.DataSource;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import be.icode.hot.data.jdbc.AbstractDB;
import be.icode.hot.data.jdbc.sql.QueryBuilder;

public class DB extends AbstractDB<PyDictionary> {

	public DB(QueryBuilder queryBuilder, DataSource dataSource, String schema) {
		super(queryBuilder, dataSource, schema);
	}

	@Override
	public JoinableCollection getCollection(String name) {
		return new JoinableCollection(new NamedParameterJdbcTemplate(dataSource), queryBuilder, name, tableMetadataMap);
	}
	
	public PyObject __getattr__(String name) {
		return Py.java2py(getCollection(name));
	}
}
