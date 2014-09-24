package be.icode.hot.data.jdbc.sql;

import java.util.Map;

import com.google.common.collect.Multimap;

public interface QueryWithCriteria extends Query {
	
	Multimap<String, Object> addWhereClauses(Map<String, Object> clauses);
}
