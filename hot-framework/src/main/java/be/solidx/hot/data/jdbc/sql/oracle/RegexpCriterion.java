package be.solidx.hot.data.jdbc.sql.oracle;

import be.solidx.hot.data.criterion.Operator;
import be.solidx.hot.data.jdbc.sql.criterion.CriterionImpl;

public class RegexpCriterion extends CriterionImpl {

	private static final String CRITERION_PATTERN = "REGEXP_LIKE (%s,:%s_%d)";
	
	public RegexpCriterion(String parameterName, int index) {
		super(Operator.$regex, parameterName, index);
	}
	
	@Override
	public String toString() {
		return String.format(CRITERION_PATTERN, parameterName,substitutionParameterName,index);
	}
}
