package be.solidx.hot.data.criterion;

import java.util.Map;

import com.google.common.collect.Multimap;

public interface CriterionFactory {

	@SuppressWarnings("rawtypes")
	Criterion buildCriteria(Map criterionMap, Multimap<String, Object> criterionValues, KeyModifier keyModifier);
	
	@SuppressWarnings("rawtypes")
	Criterion buildCriteria(Map criterionMap, Multimap<String, Object> criterionValues);
	
	@SuppressWarnings("rawtypes")
	Criterion buildCriteria(Map criterionMap);
}