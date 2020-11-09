package be.solidx.hot.data.jdbc.sql.impl;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.criterion.CriterionFactory;
import be.solidx.hot.data.criterion.Operator;
import be.solidx.hot.data.jdbc.TableMetadata;
import be.solidx.hot.data.jdbc.sql.QueryWithCriteria;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionGroup;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;
import be.solidx.hot.data.jdbc.sql.criterion.ModuloCriterion;
import be.solidx.hot.data.jdbc.sql.criterion.MultivaluedCriterion;
import be.solidx.hot.data.jdbc.sql.criterion.SqlCriterion;
import be.solidx.hot.data.jdbc.sql.criterion.TablenameKeyModifier;

import com.google.common.collect.Multimap;


abstract public class AbstractQueryWithCriteria extends AbstractQuery implements QueryWithCriteria {

	private CriterionFactory criterionFactory;
	
	public AbstractQueryWithCriteria(TableMetadata tableMetadata, CriterionFactory criterionFactory) {
		super(tableMetadata);
		this.criterionFactory = criterionFactory;
	}

	@Override
	public Multimap<String, Object> addWhereClauses(Map<String, Object> clauses) {
		rootCriterionGroup = criterionFactory.buildCriteria(clauses, criterionValues, new TablenameKeyModifier(tableMetadata));
		return criterionValues;
	}
	
	@SuppressWarnings("unchecked")
	private Criterion addCriterion (Map<String, Object> criterionMap) {
		List<Criterion> parentCriteria = new ArrayList<Criterion>();
		for (String key : criterionMap.keySet()) {
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
						if (subCriterionMap instanceof Map<?, ?>) {
							criteria.add(addCriterion((Map<String, Object>)subCriterionMap));
						}
					}
					parentCriteria.add(new CriterionGroup(operator == Operator.$and, criteria));
				}
				//TODO WARNING
			} else {
				String realKey = key;
				if (realKey.split("\\.").length < 2) realKey = tableMetadata.getName() + "." + realKey;
				realKey = withSchema(realKey);
				List<Criterion> criteria = new ArrayList<Criterion>();
				if (value instanceof Map<?, ?>) {
					Map<String, Object> subcriterionMap = (Map<String, Object>) value;
					for (String criterionOperatorString : subcriterionMap.keySet()) {
						try {
							Operator criterionOperator = Operator.fromValue(criterionOperatorString);
							Criterion criterion = null;
							if (subcriterionMap.get(criterionOperatorString) instanceof List<?>) {
								List<Object> values = (List<Object>) subcriterionMap.get(criterionOperatorString);
								if (criterionOperator == Operator.$mod) {
									criterion = buildModuloCriterion(realKey,criterionValues.get(realKey).size());
								} else if (criterionOperator == Operator.$in 
										|| criterionOperator == Operator.$nin) {
									criterion = new MultivaluedCriterion(criterionOperator, realKey,criterionValues.get(realKey).size(),values.size());
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
									criterion = buildRegexpCriterion(realKey,criterionValues.get(realKey).size());
								} else if (criterionOperator == Operator.$like) {
									parameterValue = (String) subcriterionMap.get(criterionOperator.name());
									criterion = new CriterionImpl(criterionOperator,realKey,criterionValues.get(realKey).size());
								} else if (criterionOperator == Operator.$where) {
									criterion = new SqlCriterion((String) subcriterionMap.get(criterionOperator.name()));
								} else {
									parameterValue = (String) subcriterionMap.get(key);
									criterion = new CriterionImpl(criterionOperator,realKey,criterionValues.get(realKey).size());
								}
								criteria.add(criterion);
								criterionValues.put(realKey, parameterValue);
							}
						} catch (IllegalArgumentException e) {
							parentCriteria.add(addCriterion((Map<String, Object>) subcriterionMap.get(criterionOperatorString)));
						}
					}
					parentCriteria.add(new CriterionGroup(true, criteria));
				} else {
					CriterionImpl whereClause = new CriterionImpl(Operator.$eq,realKey,criterionValues.get(realKey).size());
					parentCriteria.add(whereClause);
					criterionValues.put(realKey, value);
				}
			}
		}
		return new CriterionGroup(true, parentCriteria);
	}
	
	protected String buildWhereClauses () {
		return " WHERE " + rootCriterionGroup;
	}
	
	protected CriterionImpl buildModuloCriterion (String parameterName, int index) {
		return new ModuloCriterion(parameterName, index);
	}
	
	abstract protected CriterionImpl buildRegexpCriterion (String parameterName, int index);
}
