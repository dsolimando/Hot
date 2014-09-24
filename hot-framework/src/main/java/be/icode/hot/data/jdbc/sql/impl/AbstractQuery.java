package be.icode.hot.data.jdbc.sql.impl;

import be.icode.hot.data.criterion.Criterion;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.Query;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

abstract public class AbstractQuery implements Query {

	protected TableMetadata tableMetadata;
	
	protected Criterion rootCriterionGroup;
	
	protected Multimap<String, Object> criterionValues = LinkedListMultimap.create();
	
	public AbstractQuery(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	protected String buildFromClause () {
		return "FROM "+ tablenameWithSchema();
	}
	
	protected String withSchema (String tablename) {
		if (tableMetadata.getSchema() != null) {
			return tableMetadata.getSchema() + "." + tablename;
		}
		return tablename;
	}
	
	protected String tablenameWithSchema () {
		if (tableMetadata.getSchema() != null) {
			return tableMetadata.getSchema() + "." + tableMetadata.getName();
		}
		return tableMetadata.getName();
	}
}
