package be.solidx.hot.data.jdbc.sql.criterion;

import be.solidx.hot.data.criterion.Criterion;
import be.solidx.hot.data.criterion.Operator;


public class CriterionImpl implements Criterion {
	
	protected static final String CRITERION_PATTERN = "%s %s %s_%d";

	protected Operator operator;
	
	protected String parameterName;
	
	protected String substitutionParameterName;
	
	protected int index;

	public CriterionImpl(Operator operator, String parameterName, int index) {
		this.operator = operator;
		this.parameterName = parameterName;
		this.substitutionParameterName = parameterName.replaceAll("\\.", "_");
		this.index = index;
	}
	
	@Override
	public String toString() {
		return String.format(CRITERION_PATTERN, parameterName, operator,":"+substitutionParameterName,index);
	}
	
	public String getSubstitutionParameterName() {
		return substitutionParameterName;
	}
}
