package be.solidx.hot.data.criterion;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractCriterionFactory<T extends Criterion> implements CriterionFactory {
	
	private static final Log logger = LogFactory.getLog(AbstractCriterionFactory.class);
	
	@SuppressWarnings("rawtypes")
	@Override
	public T buildCriteria(Map criterionMap, Multimap<String, Object> criterionValues) {
		return buildCriteria(criterionMap, criterionValues,null);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public T buildCriteria(Map criterionMap) {
		return buildCriteria(criterionMap, LinkedListMultimap.<String,Object>create() , null);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public T buildCriteria (Map criterionMap, Multimap<String, Object> criterionValues, KeyModifier keyModifier) {
		List<T> parentCriteria = new ArrayList<T>();
		for (Object keyObject : criterionMap.keySet()) {
			if (!(keyObject instanceof String)) {
				logger.warn("Found criteria with non String name: "+keyObject);
				continue;
			}
			String key = (String) keyObject;
			Object value = criterionMap.get(key);
			Operator operator = null;
			if (key.equals(Operator.$and) || key.equals(Operator.$and.name())) {
				operator = Operator.$and;
			} else if (key.equals(Operator.$or) || key.equals(Operator.$or.name())) {
				operator = Operator.$or;
			}
			if (operator != null) {
				if (value instanceof List<?>) {
					List<?> criterionList = (List<?>) value;
					List<Criterion> criteria = new ArrayList<Criterion>();
					for (Object subCriterionMap : criterionList) {
						if (subCriterionMap instanceof Map) {
							criteria.add(buildCriteria((Map)subCriterionMap,criterionValues, keyModifier));
						}
					}
					parentCriteria.add(buildCriterionGroup(operator == Operator.$and, criteria));
				}
				//TODO WARNING
			} else {
				String realKey = key;
				if (keyModifier != null) realKey = keyModifier.modifyKey(key);
				List<Criterion> criteria = new ArrayList<Criterion>();
				if (value instanceof Map) {
					Map subcriterionMap = (Map) value;
					for (Object criterionOperatorObject : subcriterionMap.keySet()) {
						if (!(criterionOperatorObject instanceof String)) {
							logger.warn("Found operator with non String name: "+criterionOperatorObject);
							continue;
						}
						try {
							Operator criterionOperator = Operator.fromValue((String) criterionOperatorObject);
							Criterion criterion = null;
							if (subcriterionMap.get(criterionOperatorObject) instanceof List<?>) {
								List<?> values = (List<?>) subcriterionMap.get(criterionOperatorObject);
								if (criterionOperator == Operator.$mod) {
									int moduloValue;
									int resultValue;
									if (values.get(0) instanceof Double) {
										moduloValue = ((Double)values.get(0)).intValue();
									} else {
										moduloValue = (Integer) values.get(0);
									}
									if (values.get(1) instanceof Double) {
										resultValue = ((Double)values.get(1)).intValue();
									} else {
										resultValue = (Integer) values.get(1);
									}
									criterion = buildModuloCriterion(realKey, moduloValue, resultValue, criterionValues.get(realKey).size());
								} else if (criterionOperator == Operator.$in 
										|| criterionOperator == Operator.$nin) {
									criterion = buildMultivaluedCriterion(criterionOperator, realKey, values, criterionValues.get(realKey).size());
								} else {
									continue;
								}
								criteria.add(criterion);
								for (int i = 1; i <= values.size(); i++) {
									criterionValues.put(realKey, values.get(i-1));
								}
							} else {
								String parameterValue = null;
								if (criterionOperator == Operator.$regex) {
									parameterValue = (String) subcriterionMap.get(criterionOperator.name());
									criterion = buildRegexpCriterion(realKey, parameterValue, criterionValues.get(realKey).size());
								} else if (criterionOperator == Operator.$like) {
									parameterValue = (String) subcriterionMap.get(criterionOperator.name());
									criterion = buildCriterion(criterionOperator,realKey,parameterValue,criterionValues.get(realKey).size());
								} else if (criterionOperator == Operator.$where) {
									criterion = buildNativeCriterion((String) subcriterionMap.get(criterionOperator.name()));
								} else {
									Object objectValue = subcriterionMap.get(criterionOperator.name());
									criterion = buildCriterion(criterionOperator,realKey,objectValue,criterionValues.get(realKey).size());
								}
								criteria.add(criterion);
								criterionValues.put(realKey, parameterValue);
							}
						} catch (IllegalArgumentException e) {
							parentCriteria.add(buildCriteria((Map) subcriterionMap.get(criterionOperatorObject),criterionValues, keyModifier));
						}
					}
					parentCriteria.add(buildCriterionGroup(true, criteria));
				} else {
					T whereClause =  buildCriterion(Operator.$eq,realKey,value,criterionValues.get(realKey).size());
					parentCriteria.add(whereClause);
					criterionValues.put(realKey, value);
				}
			}
		}
		return buildCriterionGroup(true, parentCriteria);
	
	}
	
	protected abstract T buildCriterion (Operator operator, String parameterName, Object value, int index);
	
	protected abstract T buildCriterionGroup (Boolean and, List<? extends Criterion> criterion);
	
	protected abstract T buildModuloCriterion (String parameterName, int moduloValue, int value, int index);
	
	protected abstract T buildMultivaluedCriterion (Operator operator, String parameterName, List<?> values, int index);
	
	protected abstract T buildRegexpCriterion (String parameterName, String value, int index);
	
	protected abstract T buildNativeCriterion (String criterion);
}
