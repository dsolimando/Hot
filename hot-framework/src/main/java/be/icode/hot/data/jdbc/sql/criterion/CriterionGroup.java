package be.icode.hot.data.jdbc.sql.criterion;

import java.util.ArrayList;
import java.util.List;

import be.icode.hot.data.criterion.Criterion;

public class CriterionGroup implements Criterion {

	private boolean and = true;
	
	private List<Criterion> criteria = new ArrayList<Criterion>();

	public CriterionGroup(boolean and, List<? extends Criterion> whereClauses) {
		this.and = and;
		this.criteria.addAll(whereClauses);
	}
	
	@Override
	public String toString() {
		String clause = "";
		if (criteria.size() > 1) clause += "(";
		String separator = "";
		for (Criterion criterion : criteria) {
			clause += String.format("%s %s", separator,criterion);
			separator = and?" AND":" OR";
		}
		if (criteria.size() > 1) clause += " )";
		return clause.trim();
	}
}
