package be.icode.hot.data.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import be.icode.hot.data.jdbc.TableMetadata.ForeignKeySet;
import be.icode.hot.data.jdbc.sql.QueryBuilder;
import be.icode.hot.exceptions.HotDBMetadataRetrievalException;

public abstract class AbstractDB<T extends Map<?,?>> implements DB<T> {

	protected QueryBuilder queryBuilder;
	protected DataSource dataSource;
	protected Map<String, TableMetadata> tableMetadataMap = new LinkedHashMap<String, TableMetadata>();
	protected String schema;

	public AbstractDB(QueryBuilder queryBuilder, DataSource dataSource, String schema) {
		this.queryBuilder = queryBuilder;
		this.dataSource = dataSource;
		this.schema = schema;
		try {
			collectMetadata();
		} catch (SQLException e) {
			throw new HotDBMetadataRetrievalException(e);
		}
	}
	
	@Override
	public abstract JoinableCollection<T> getCollection(String name);
	
	@Override
	public List<String> listCollections() {
		return new ArrayList<String>(tableMetadataMap.keySet());
	}

	@Override
	public TableMetadata getCollectionMetadata(String name) {
		return tableMetadataMap.get(name);
	}
	
	@Override
	public List<String> getPrimaryKeys(String collection) {
		return getCollectionMetadata(collection).getPrimaryKeys();
	}
	
	public Map<String, TableMetadata> getTableMetadataMap() {
		return new HashMap<String, TableMetadata>(tableMetadataMap);
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	private void collectMetadata() throws SQLException {
		Connection connection = dataSource.getConnection();
		String upperSchema = schema != null? schema.toUpperCase():null;
		ResultSet resultSet = connection.getMetaData().getTables(null, upperSchema, "%", new String[] {"TABLE"});
		
		while (resultSet.next()) {
			Map<String, ForeignKeySet> 
			importedPKsMap = new LinkedHashMap<String, TableMetadata.ForeignKeySet>(), 
			exportedFKsMap = new LinkedHashMap<String, TableMetadata.ForeignKeySet>();
			String tablename = resultSet.getString("TABLE_NAME");
			
			// Retrieving Column Names
			ResultSet columnsRS = connection.getMetaData().getColumns(null, upperSchema, tablename, "%");
			List<String> columns = new ArrayList<String>();
			while (columnsRS.next()) {
				columns.add(columnsRS.getString("COLUMN_NAME").toLowerCase());
			}
			columnsRS.close();
			
			// Retrieving Primary Keys
			ResultSet pkRS = connection.getMetaData().getPrimaryKeys(null, upperSchema, tablename);
			List<String> primaryKeys = new ArrayList<String>();
			while (pkRS.next()) {
				primaryKeys.add(pkRS.getString("COLUMN_NAME").toLowerCase());
			}
			pkRS.close();
			
			// Retrieving exported Foreign Keys
			ResultSet infos = connection.getMetaData().getExportedKeys(null, upperSchema, tablename);
			String fkTablename = null, newFkTablename = null;
			List<String> fkNames = new ArrayList<String>(), pkNames = new ArrayList<String>();
			while (infos.next()) {
				newFkTablename = infos.getString("FKTABLE_NAME").toLowerCase();
				if (!newFkTablename.equals(fkTablename) && fkTablename != null) {
					exportedFKsMap.put(fkTablename, new ForeignKeySet(schema, fkTablename, pkNames, fkNames)) ;
					fkNames.clear();
					pkNames.clear();
				}
				fkNames.add(infos.getString("FKCOLUMN_NAME").toLowerCase());
				pkNames.add(infos.getString("PKCOLUMN_NAME").toLowerCase());
				fkTablename = newFkTablename;
			}
			if (fkTablename != null)
				exportedFKsMap.put(fkTablename, new ForeignKeySet(schema, fkTablename, pkNames, fkNames)) ;
			infos.close();
			
			// Retrieving Imported Primary Keys
			infos = connection.getMetaData().getImportedKeys(null, upperSchema, tablename);
			String pkTablename = null, newPkTablename = null;
			fkNames.clear();
			pkNames.clear();
			while (infos.next()) {
				newPkTablename = infos.getString("PKTABLE_NAME").toLowerCase();
				if (!newPkTablename.equals(pkTablename) && pkTablename != null) {
					importedPKsMap.put(pkTablename, new ForeignKeySet(schema, pkTablename, pkNames, fkNames));
					fkNames.clear();
					pkNames.clear();
				}
				fkNames.add(infos.getString("FKCOLUMN_NAME").toLowerCase());
				pkNames.add(infos.getString("PKCOLUMN_NAME").toLowerCase());
				pkTablename = newPkTablename;
			}
			if (pkTablename != null)
				importedPKsMap.put(pkTablename, new ForeignKeySet(schema, pkTablename, pkNames, fkNames));
			infos.close();
			tableMetadataMap.put(tablename.toLowerCase(), new TableMetadata(tablename.toLowerCase(), schema, columns, primaryKeys, importedPKsMap, exportedFKsMap));
		}
	}
}
