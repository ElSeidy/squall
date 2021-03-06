package plan_runner.query_plans;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import plan_runner.components.DataSourceComponent;
import plan_runner.components.EquiJoinComponent;
import plan_runner.conversion.DateConversion;
import plan_runner.conversion.DoubleConversion;
import plan_runner.conversion.NumericConversion;
import plan_runner.conversion.StringConversion;
import plan_runner.conversion.TypeConversion;
import plan_runner.expressions.ColumnReference;
import plan_runner.expressions.IntegerYearFromDate;
import plan_runner.expressions.Multiplication;
import plan_runner.expressions.Subtraction;
import plan_runner.expressions.ValueExpression;
import plan_runner.expressions.ValueSpecification;
import plan_runner.operators.AggregateOperator;
import plan_runner.operators.AggregateSumOperator;
import plan_runner.operators.ProjectOperator;
import plan_runner.operators.SelectOperator;
import plan_runner.predicates.BetweenPredicate;
import plan_runner.predicates.ComparisonPredicate;

public class TPCH8Plan {
	private static Logger LOG = Logger.getLogger(TPCH8Plan.class);

	private final QueryPlan _queryPlan = new QueryPlan();

	private static final String _region = "AMERICA";
	private static final String _type = "ECONOMY ANODIZED STEEL";
	private static final String _date1Str = "1995-01-01";
	private static final String _date2Str = "1996-12-31";

	private static final TypeConversion<Date> _dateConv = new DateConversion();
	private static final NumericConversion<Double> _doubleConv = new DoubleConversion();
	private static final TypeConversion<String> _sc = new StringConversion();

	private static final Date _date1 = _dateConv.fromString(_date1Str);
	private static final Date _date2 = _dateConv.fromString(_date2Str);

	public TPCH8Plan(String dataPath, String extension, Map conf) {
		// -------------------------------------------------------------------------------------
		final List<Integer> hashRegion = Arrays.asList(0);

		final SelectOperator selectionRegion = new SelectOperator(new ComparisonPredicate(
				new ColumnReference(_sc, 1), new ValueSpecification(_sc, _region)));

		final ProjectOperator projectionRegion = new ProjectOperator(new int[] { 0 });

		final DataSourceComponent relationRegion = new DataSourceComponent("REGION", dataPath
				+ "region" + extension, _queryPlan).setHashIndexes(hashRegion)
				.addOperator(selectionRegion).addOperator(projectionRegion);

		// -------------------------------------------------------------------------------------
		final List<Integer> hashNation1 = Arrays.asList(1);

		final ProjectOperator projectionNation1 = new ProjectOperator(new int[] { 0, 2 });

		final DataSourceComponent relationNation1 = new DataSourceComponent("NATION1", dataPath
				+ "nation" + extension, _queryPlan).setHashIndexes(hashNation1).addOperator(
				projectionNation1);

		// -------------------------------------------------------------------------------------
		final EquiJoinComponent R_Njoin = new EquiJoinComponent(relationRegion, relationNation1,
				_queryPlan).addOperator(new ProjectOperator(new int[] { 1 })).setHashIndexes(
				Arrays.asList(0));

		// -------------------------------------------------------------------------------------
		final List<Integer> hashCustomer = Arrays.asList(0);

		final ProjectOperator projectionCustomer = new ProjectOperator(new int[] { 3, 0 });

		final DataSourceComponent relationCustomer = new DataSourceComponent("CUSTOMER", dataPath
				+ "customer" + extension, _queryPlan).setHashIndexes(hashCustomer).addOperator(
				projectionCustomer);

		// -------------------------------------------------------------------------------------
		final EquiJoinComponent R_N_Cjoin = new EquiJoinComponent(R_Njoin, relationCustomer,
				_queryPlan).addOperator(new ProjectOperator(new int[] { 1 })).setHashIndexes(
				Arrays.asList(0));

		// -------------------------------------------------------------------------------------
		final List<Integer> hashSupplier = Arrays.asList(1);

		final ProjectOperator projectionSupplier = new ProjectOperator(new int[] { 0, 3 });

		final DataSourceComponent relationSupplier = new DataSourceComponent("SUPPLIER", dataPath
				+ "supplier" + extension, _queryPlan).setHashIndexes(hashSupplier).addOperator(
				projectionSupplier);

		// -------------------------------------------------------------------------------------
		final List<Integer> hashNation2 = Arrays.asList(0);

		final ProjectOperator projectionNation2 = new ProjectOperator(new int[] { 0, 1 });

		final DataSourceComponent relationNation2 = new DataSourceComponent("NATION2", dataPath
				+ "nation" + extension, _queryPlan).setHashIndexes(hashNation2).addOperator(
				projectionNation2);

		// -------------------------------------------------------------------------------------
		final EquiJoinComponent S_Njoin = new EquiJoinComponent(relationSupplier, relationNation2,
				_queryPlan).addOperator(new ProjectOperator(new int[] { 0, 2 })).setHashIndexes(
				Arrays.asList(0));

		// -------------------------------------------------------------------------------------
		final List<Integer> hashPart = Arrays.asList(0);

		final SelectOperator selectionPart = new SelectOperator(new ComparisonPredicate(
				new ColumnReference(_sc, 4), new ValueSpecification(_sc, _type)));

		final ProjectOperator projectionPart = new ProjectOperator(new int[] { 0 });

		final DataSourceComponent relationPart = new DataSourceComponent("PART", dataPath + "part"
				+ extension, _queryPlan).setHashIndexes(hashPart).addOperator(selectionPart)
				.addOperator(projectionPart);

		// -------------------------------------------------------------------------------------
		final List<Integer> hashLineitem = Arrays.asList(1);

		// first field in projection
		final ColumnReference orderKey = new ColumnReference(_sc, 0);
		// second field in projection
		final ColumnReference partKey = new ColumnReference(_sc, 1);
		// third field in projection
		final ColumnReference suppKey = new ColumnReference(_sc, 2);
		// forth field in projection
		final ValueExpression<Double> substract = new Subtraction(new ValueSpecification(
				_doubleConv, 1.0), new ColumnReference(_doubleConv, 6));
		// extendedPrice*(1-discount)
		final ValueExpression<Double> product = new Multiplication(new ColumnReference(_doubleConv,
				5), substract);
		final ProjectOperator projectionLineitem = new ProjectOperator(orderKey, partKey, suppKey,
				product);

		final DataSourceComponent relationLineitem = new DataSourceComponent("LINEITEM", dataPath
				+ "lineitem" + extension, _queryPlan).setHashIndexes(hashLineitem).addOperator(
				projectionLineitem);

		// -------------------------------------------------------------------------------------
		final EquiJoinComponent P_Ljoin = new EquiJoinComponent(relationPart, relationLineitem,
				_queryPlan).addOperator(new ProjectOperator(new int[] { 1, 2, 3 })).setHashIndexes(
				Arrays.asList(0));

		// -------------------------------------------------------------------------------------
		final List<Integer> hashOrders = Arrays.asList(0);

		final SelectOperator selectionOrders = new SelectOperator(new BetweenPredicate(
				new ColumnReference(_dateConv, 4), true, new ValueSpecification(_dateConv, _date1),
				true, new ValueSpecification(_dateConv, _date2)));

		// first field in projection
		final ValueExpression OrdersOrderKey = new ColumnReference(_sc, 0);
		// second field in projection
		final ValueExpression OrdersCustKey = new ColumnReference(_sc, 1);
		// third field in projection
		final ValueExpression OrdersExtractYear = new IntegerYearFromDate(
				new ColumnReference<Date>(_dateConv, 4));
		final ProjectOperator projectionOrders = new ProjectOperator(OrdersOrderKey, OrdersCustKey,
				OrdersExtractYear);

		final DataSourceComponent relationOrders = new DataSourceComponent("ORDERS", dataPath
				+ "orders" + extension, _queryPlan).setHashIndexes(hashOrders)
				.addOperator(selectionOrders).addOperator(projectionOrders);

		// -------------------------------------------------------------------------------------
		final EquiJoinComponent P_L_Ojoin = new EquiJoinComponent(P_Ljoin, relationOrders,
				_queryPlan).addOperator(new ProjectOperator(new int[] { 1, 2, 3, 4 }))
				.setHashIndexes(Arrays.asList(0));

		// -------------------------------------------------------------------------------------
		final EquiJoinComponent S_N_P_L_Ojoin = new EquiJoinComponent(S_Njoin, P_L_Ojoin,
				_queryPlan).addOperator(new ProjectOperator(new int[] { 1, 2, 3, 4 }))
				.setHashIndexes(Arrays.asList(2));

		// -------------------------------------------------------------------------------------
		final AggregateOperator agg = new AggregateSumOperator(new ColumnReference(_doubleConv, 2),
				conf).setGroupByColumns(Arrays.asList(1, 3));

		new EquiJoinComponent(R_N_Cjoin, S_N_P_L_Ojoin, _queryPlan).addOperator(agg);

		// -------------------------------------------------------------------------------------

	}

	public QueryPlan getQueryPlan() {
		return _queryPlan;
	}
}