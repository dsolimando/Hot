package be.solidx.hot.data.jdbc.sql.criterion;

import be.solidx.hot.data.criterion.KeyModifier;
import be.solidx.hot.data.jdbc.TableMetadata;

public class TablenameKeyModifier implements KeyModifier {

	private TableMetadata tableMetadata;
	
	public TablenameKeyModifier(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	@Override
	public String modifyKey(String key) {
		if (key.split("\\.").length < 2) 
			key = tableMetadata.getName() + "." + key;
		
		if (tableMetadata.getSchema() != null)
			return tableMetadata.getSchema() + "." + key;
		return key;
	}
}
