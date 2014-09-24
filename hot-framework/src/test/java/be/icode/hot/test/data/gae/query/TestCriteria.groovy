package be.icode.hot.test.data.gae.query

import org.junit.Test

import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import be.icode.hot.data.criterion.CriterionCreationException;
import be.icode.hot.data.gae.query.GaeCriterionFactory
import be.icode.hot.data.gae.query.GaeCriterionGroup;
import be.icode.hot.data.gae.query.GaeCriterionImpl;

class TestCriteria {

	GaeCriterionFactory criterionFactory = new GaeCriterionFactory()
	
	@Test
	void testCriteria1 () {
		def criteria = [id:6,first_name:"Jean"]
		GaeCriterionGroup criterion = criterionFactory.buildCriteria(criteria)
		
		assert criterion.getFilter().operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters.size() == 2
		assert criterion.getFilter().subFilters[0] instanceof FilterPredicate
		assert criterion.getFilter().subFilters[0].propertyName == "id"
		assert criterion.getFilter().subFilters[0].value == 6
		assert criterion.getFilter().subFilters[0].filterOperator == FilterOperator.EQUAL
		assert criterion.getFilter().subFilters[1] instanceof FilterPredicate
		assert criterion.getFilter().subFilters[1].propertyName == "first_name"
		assert criterion.getFilter().subFilters[1].value == "Jean"
		assert criterion.getFilter().subFilters[1].filterOperator == FilterOperator.EQUAL
	}
	
	@Test
	void testCriteria2() {
		def criteria = [
			id:  [ $ne:1, $gt:10],
			$or:  [
					[
						name: "Damien",
						good:	true
					],
					[
						"age": [$lt:10]
					]
			  ]
		   ]
		GaeCriterionGroup criterion = criterionFactory.buildCriteria(criteria)
		
		assert criterion.getFilter().operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters.size() == 2
		assert criterion.getFilter().subFilters[0] instanceof CompositeFilter
		assert criterion.getFilter().subFilters[1] instanceof CompositeFilter
		assert criterion.getFilter().subFilters[0].subFilters.size() == 2
		assert criterion.getFilter().subFilters[0].subFilters[0].propertyName == "id"
		assert criterion.getFilter().subFilters[0].subFilters[0].filterOperator == FilterOperator.NOT_EQUAL
		assert criterion.getFilter().subFilters[0].subFilters[0].value == 1
		assert criterion.getFilter().subFilters[0].subFilters[1].propertyName == "id"
		assert criterion.getFilter().subFilters[0].subFilters[1].filterOperator == FilterOperator.GREATER_THAN
		assert criterion.getFilter().subFilters[0].subFilters[1].value == 10
		
		assert criterion.getFilter().subFilters[1].operator == CompositeFilterOperator.OR
		assert criterion.getFilter().subFilters[1].subFilters[0] instanceof CompositeFilter
		assert criterion.getFilter().subFilters[1].subFilters[0].operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters[1].subFilters[0].subFilters[0].propertyName == "name"
		assert criterion.getFilter().subFilters[1].subFilters[0].subFilters[0].value == "Damien"
		assert criterion.getFilter().subFilters[1].subFilters[0].subFilters[0].filterOperator == FilterOperator.EQUAL
		assert criterion.getFilter().subFilters[1].subFilters[0].subFilters[1].propertyName == "good"
		assert criterion.getFilter().subFilters[1].subFilters[0].subFilters[1].value == true
		assert criterion.getFilter().subFilters[1].subFilters[0].subFilters[1].filterOperator == FilterOperator.EQUAL
		assert criterion.getFilter().subFilters[1].subFilters[1].subFilters[0] instanceof CompositeFilter
		assert criterion.getFilter().subFilters[1].subFilters[1].subFilters[0].subFilters[0].propertyName == "age"
		assert criterion.getFilter().subFilters[1].subFilters[1].subFilters[0].subFilters[0].value == 10
		assert criterion.getFilter().subFilters[1].subFilters[1].subFilters[0].subFilters[0].filterOperator == FilterOperator.LESS_THAN
	}
	
	@Test
	void testIn () {
		def criteria = ["age" : [$in:[30,31,32,33,34]]]
		GaeCriterionGroup criterion = criterionFactory.buildCriteria(criteria)
		
		assert criterion.getFilter().operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters.size() == 1
		assert criterion.getFilter().subFilters[0].operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters[0].subFilters.size() == 1
		assert criterion.getFilter().subFilters[0].subFilters[0].filterOperator == FilterOperator.IN
		assert criterion.getFilter().subFilters[0].subFilters[0].propertyName == "age"
		assert criterion.getFilter().subFilters[0].subFilters[0].value == [30,31,32,33,34]
	}
	
	@Test
	void testNin () {
		def criteria = ["age" : [$nin:[30,31,32,33,34]]]
		GaeCriterionGroup criterion = criterionFactory.buildCriteria(criteria)
		assert criterion.getFilter().operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters.size() == 1
		assert criterion.getFilter().subFilters[0].operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters[0].subFilters.size() == 1
		assert criterion.getFilter().subFilters[0].subFilters[0] instanceof CompositeFilter
		assert criterion.getFilter().subFilters[0].subFilters[0].operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters.size() == 5
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[0].propertyName == "age"
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[0].value == 30
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[0].filterOperator == FilterOperator.NOT_EQUAL
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[1].propertyName == "age"
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[1].value == 31
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[1].filterOperator == FilterOperator.NOT_EQUAL
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[2].propertyName == "age"
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[2].value == 32
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[2].filterOperator == FilterOperator.NOT_EQUAL
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[3].propertyName == "age"
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[3].value == 33
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[3].filterOperator == FilterOperator.NOT_EQUAL
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[4].propertyName == "age"
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[4].value == 34
		assert criterion.getFilter().subFilters[0].subFilters[0].subFilters[4].filterOperator == FilterOperator.NOT_EQUAL
	}
	
	@Test(expected=CriterionCreationException.class)
	void testNinMod () {
		def criteria = ["age" : [
					$nin:[30,31,32,33,34],
					$mod:[10,0]
					]
				]
		GaeCriterionGroup criterion = criterionFactory.buildCriteria(criteria)
	}
	
	@Test
	void testLike () {
		def criteria = ["name" :[$like: '%mien%']]
		GaeCriterionGroup criterion = criterionFactory.buildCriteria(criteria)
		assert criterion.getFilter().operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters[0].operator == CompositeFilterOperator.AND
		assert criterion.getFilter().subFilters[0].subFilters[0].filterOperator == FilterOperator.EQUAL
		assert criterion.getFilter().subFilters[0].subFilters[0].propertyName == "name"
		assert criterion.getFilter().subFilters[0].subFilters[0].value == "%mien%"
	}
}
