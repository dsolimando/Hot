package be.solidx.hot.data.jdbc.sql.criterion;

import be.solidx.hot.data.criterion.Operator;


public class MultivaluedCriterion extends CriterionImpl {
	
	private static final String CRITERION_PATTERN = "%s %s (%s)";
	private static final String MULTIVALUED_IN_PATTERN = "%s :%s_%d";

	private String parameterNameList = "";
	
	public MultivaluedCriterion(Operator operator, String parameterName, int index, int numParameters) {
		super(operator, parameterName, index);
		String separator = "";
		for (int i = 0; i < numParameters; i++) {
			parameterNameList += String.format(MULTIVALUED_IN_PATTERN, separator,getSubstitutionParameterName(),index+i);
			separator = ",";
		}
	}

	@Override
	public String toString() {
		return String.format(CRITERION_PATTERN, parameterName,operator,parameterNameList);
	}
}
