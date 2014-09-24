package be.icode.hot.data.jdbc.sql.db2;

import java.util.Map;

import be.icode.hot.data.criterion.CriterionFactory;
import be.icode.hot.data.jdbc.AbstractJoinedEntity;
import be.icode.hot.data.jdbc.TableMetadata;
import be.icode.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.icode.hot.data.jdbc.sql.impl.AbstractSelectQuery;
import be.icode.hot.data.jdbc.sql.mysql.RegexpCriterion;

public class DB2SelectQuery extends AbstractSelectQuery {
	
	protected final String ROWNUM = "rn";
	
	public DB2SelectQuery(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata, criterionFactory);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected String selectOrCount() {
		if (count) {
			return "SELECT COUNT(*) ";
		} else {
			if (skip != null) {
				String pKeys = "";
				String separator = "";
				for (String pk : tableMetadata.getPrimaryKeys()) {
					pKeys += separator + tablenameWithSchema() + "." + pk + " ASC";
					separator = ",";
				}
				separator = "";
				String tables = "";
				if (joinTree != null) {
					for (AbstractJoinedEntity<? extends Map> joinedEntity : joinTree.asMap().values()) {
						for (String  column : joinedEntity.getColumnNames()) {
							tables += String.format("%s %s.%s AS %s_%s", 
									separator,
									withSchema(joinedEntity.getTableName()),
									column,
									joinedEntity.getTableName(),
									column);
							separator = ",";
						}
					}
				} else {
					tables = String.format(" %s.*", tablenameWithSchema()); 
				}
				return "SELECT ROW_NUMBER() OVER (ORDER BY "+ pKeys  +") AS "+ROWNUM+","+tables+" ";
			} else {
				return "SELECT * ";
			}
		}
	}

	@Override
	public String build() {
		String query = null;
		if (joinTree == null) {
			query = selectOrCount() + buildFromClause();
		} else {
			query = selectOrCount() + buildLeftOuterJoin();
		}
		if (rootCriterionGroup != null) {
			query += buildWhereClauses();
		}
		if (count) {
			return query.trim();
		}
		if (!sortMap.keySet().isEmpty()) {
			query += buildOrderBy();
		}
		if (limit != null || skip != null) {
			if (skip != null) {
				String finalQuery = String.format("SELECT * FROM (%s) WHERE", query);
				finalQuery += String.format(" %s > %s", ROWNUM, skip);
				if (limit != null) {
					finalQuery += String.format(" AND %s <= %s ", ROWNUM, limit+skip);
				}
				return finalQuery;
			} else {
				return query + String.format(" FETCH FIRST %d ROWS ONLY", limit);
			}
		}
		return query.trim();
	}

	@Override
	protected CriterionImpl buildRegexpCriterion(String parameterName, int index) {
		return new RegexpCriterion(parameterName, index);
	}
	
	@Override
	protected CriterionImpl buildModuloCriterion(String parameterName, int index) {
		return new ModuloCriterion(parameterName, index);
	};
}
