package be.solidx.hot.data.criterion;

public enum Operator {
	
	$eq("="), 		// equal
	$ne("!="), 		// not Equal,
	$gt(">"), 		// greater than
	$lt("<"), 		// lower than
	$gte(">="),		// greater than or equal
	$lte("<="),		// lower than or equal
	$mod("%"),		// modulo
	$in("IN"),		
	$nin("NOT IN"),
	$regex("$regex"),
	$like("LIKE"),
	$or("$or"),
	$and("$and"),
	$where("$where");
	
	private String stringValue;
	
	private Operator () {}
	
	private Operator (String value) {
		this.stringValue = value;
	}
	
	@Override
	public String toString() {
		return stringValue;
	}
	
	public static Operator fromValue(String value) {
		if (value.equals($eq) || value.equals($eq.name())) {
			return $eq;
		} else if (value.equals($ne) || value.equals($ne.name())) {
			return $ne;
		} else if (value.equals($gt) || value.equals($gt.name())) {
			return $gt;
		} else if (value.equals($lt) || value.equals($lt.name())) {
			return $lt;
		} else if (value.equals($gte) || value.equals($gte.name())) {
			return $gte;
		} else if (value.equals($lte) || value.equals($lte.name())) {
			return $lte;
		} else if (value.equals($mod) || value.equals($mod.name())) {
			return $mod;
		} else if (value.equals($in) || value.equals($in.name())) {
			return $in;
		} else if (value.equals($nin) || value.equals($nin.name())) {
			return $nin;
		} else {
			return Operator.valueOf(value);
		}
	}
}
