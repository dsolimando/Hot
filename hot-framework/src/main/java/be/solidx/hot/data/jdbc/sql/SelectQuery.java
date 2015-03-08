package be.solidx.hot.data.jdbc.sql;

import java.util.Map;

import be.solidx.hot.data.jdbc.AbstractJoinTree;

public interface SelectQuery extends QueryWithCriteria {

	Map<String, Object> getSortMap();

	SelectQuery limit(Integer limit);
	
	SelectQuery skip(Integer skip);

	SelectQuery count();
	
	@SuppressWarnings("rawtypes")
	SelectQuery addJoins (AbstractJoinTree<? extends Map> joinTree);
}