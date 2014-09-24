package be.icode.hot.data.jdbc.sql.criterion;

import be.icode.hot.data.criterion.Operator;


public class ModuloCriterion extends CriterionImpl {
	
	private static final String CRITERION_PATTERN = "MOD(%s,:%s_%d) = :%s_%d";
	
	public ModuloCriterion(String parameterName, int index) {
		super(Operator.$mod, parameterName,index);
	}
	
	@Override
	public String toString() {
		return String.format(CRITERION_PATTERN, parameterName,substitutionParameterName,index,substitutionParameterName,index+1);
	}
}
