package no.valg.eva.admin.backend.util;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

/** 
* Example of usages. Do clean up if necessary.
*
* 	static final String MV_AREA_PK = "mvAreaPk";
*	static final String OPERATOR_IDS = "operatorIds";
*
*	static Query buildOperatorRolesInAreaQuery(EntityManager em, MvArea mvArea) {
*		return em.createNativeQuery(
*				createNativeSqlBuilder()
*					.select("opr.*").from("mv_area mva")
*						.join("mv_area mva2 ON (public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))")
*						.join("operator_role opr on mva2.mv_area_pk = opr.mv_area_pk")
*						.join("operator o on opr.operator_pk = o.operator_pk")
*					.where("mva.mv_area_pk = :" + MV_AREA_PK)
*					.build(), OperatorRole.class)
*				.setParameter(MV_AREA_PK, mvArea.getPk());
*	}
*	
*	
*	static Query buildOperatorRolesInAreaAndPkListQuery(EntityManager em, MvArea mvArea, List<Long> operatorPks, boolean orderByNameAscending) {
*		return em.createNativeQuery(
*				createNativeSqlBuilder()
*					.select("opr.*").from("mv_area mva")
*						.join("mv_area mva2 ON (public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))")
*						.join("operator_role opr on mva2.mv_area_pk = opr.mv_area_pk")
*						.join("operator o on opr.operator_pk = o.operator_pk")
*					.where("mva.mv_area_pk = :" + MV_AREA_PK).where("o.operator_pk IN :" + OPERATOR_IDS)
*					.orderBy("o.name_line", orderByNameAscending)
*					.build(), OperatorRole.class)
*				.setParameter(MV_AREA_PK, mvArea.getPk())
*				.setParameter(OPERATOR_IDS, operatorPks);
*	}
*	
*	static Query queryBuilder(EntityManager em, OperatorPaginationRequest pagReg) {
*		
*		final Map<String, Object> params = new HashMap<>();
*		
*		final NativeSqlBuilder sql = createNativeSqlBuilder()
*				.from("mv_area mva")
*					.join("mv_area mva2 ON (public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))")
*					.join("operator_role opr on mva2.mv_area_pk = opr.mv_area_pk")
*					.join("operator o on opr.operator_pk = o.operator_pk")
*				.where("mva.mv_area_pk = :" + MV_AREA_PK)
*				.groupBy("o.operator_pk");
*		
*		if (pagReg.isQueryOperatorId()) {
*			sql.where("o.operator_id = :" + OPERATOR__OPERATOR_ID);
*			params.put(OPERATOR__OPERATOR_ID, pagReg.getQuery());
*		}
*		else if (pagReg.hasQuery()) {
*			sql.where("o.name_line like '%:" + OPERATOR__NAME_LINE + "%'");
*			params.put(OPERATOR__NAME_LINE, pagReg.getQuery());
*		}
*		if (pagReg.getFilteringRole() != null) {
*			sql.where("opr.role_pk = :" + OPERATOR_ROLE__ROLE_PK);
*			params.put(OPERATOR_ROLE__ROLE_PK, pagReg.getFilteringRole().getRoleId());
*		}
*		if (pagReg.getFilteringArea() != null)  {
*			sql.where("opr.mv_area_pk = :" + OPERATOR_ROLE__MV_AREA_PK);
*			params.put(OPERATOR_ROLE__MV_AREA_PK, pagReg.getFilteringArea().getId());
*		}
*		
*		final Query q = em.createNativeQuery(sql.build(), Long.class);
*		params.forEach(q::setParameter);
*		return q;
*	} 
*/

public class NativeSqlBuilder {

	private static final String DELIMITER_COMMA = ",";
	private static final String DELIMITER_AND = " AND";
	private static final String DELIMITER_JOIN = " JOIN";

	private final List<String> selects = new ArrayList<>();
	private final List<String> froms = new ArrayList<>();
	private final List<String> joins = new ArrayList<>();
	private final List<String> wheres = new ArrayList<>();
	private final List<String> groupBys = new ArrayList<>();
	private final List<String> orderBys = new ArrayList<>();
	private final StringBuilder sqlBuilder = new StringBuilder();
	
	public static NativeSqlBuilder createNativeSqlBuilder() {
		return new NativeSqlBuilder();
	}

	private static void requireNotEmpty(List<String> list) {
		if (list == null || list.isEmpty()) {
			throw new IllegalArgumentException("Missing required statement");
		}
	}

	private static boolean notEmpty(List<String> list) {
		return !list.isEmpty();
	}

	public NativeSqlBuilder select(String argument) {
		addIfNotExists(selects, argument);
		return this;
	}

	private static void addIfNotExists(List<String> list, String value) {
		if (notExists(list, value)) {
			list.add(value);
		}
	}

	private static boolean notExists(List<String> list, String value) {
		return !exists(list, value);
	}

	private static boolean exists(List<String> list, String value) {
		for (String v : list) {
			if (v.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public NativeSqlBuilder from(String argument) {
		addIfNotExists(froms, argument);
		return this;
	}

	public NativeSqlBuilder join(String argument) {
		addIfNotExists(joins, argument);
		return this;
	}

	public NativeSqlBuilder where(String argument) {
		addIfNotExists(wheres, argument);
		return this;
	}

	public NativeSqlBuilder groupBy(String argument) {
		addIfNotExists(groupBys, argument);
		return this;
	}

	public NativeSqlBuilder orderBy(String argument) {
		return orderBy(argument, true);
	}

	public NativeSqlBuilder orderBy(String argument, boolean ascending) {
		orderBys.add(argument + " " + toOrderByAscending(ascending));
		return this;
	}

	private static String toOrderByAscending(boolean ascending) {
		return ascending ? "ASC" : "DESC";
	}

	public String build() {
		buildSelectStatement();
		buildFromStatement();
		buildJoinStatement();
		buildWhereStatement();
		buildGroupByStatement();
		buildOrderByStatement();
		return toSql();
	}

	private void buildSelectStatement() {
		requireNotEmpty(selects);
		append("SELECT");
		appendList(selects);
	}

	private void appendList(List<String> list) {
		appendList(list, DELIMITER_COMMA);
	}

	private void appendList(List<String> list, String delimiter) {
		for (int i = 0; i < list.size(); i++) {
			final String s = list.get(i);
			append(s);
			if (i < list.size() - 1) {
				sqlBuilder.append(delimiter);
			}
		}
	}

	private void append(String sql) {
		if (isNotEmpty(sql)) {
			if (sqlBuilder.length() > 0) {
				sqlBuilder.append(" ");
			}
			sqlBuilder.append(sql);
		}
	}

	private void buildFromStatement() {
		requireNotEmpty(froms);
		append("FROM");
		appendList(froms);
	}

	private void buildJoinStatement() {
		if (notEmpty(joins)) {
			append("JOIN");
			appendList(joins, DELIMITER_JOIN);
		}
	}

	private void buildWhereStatement() {
		if (notEmpty(wheres)) {
			append("WHERE");
			appendList(wheres, DELIMITER_AND);
		}
	}

	private void buildGroupByStatement() {
		if (notEmpty(groupBys)) {
			append("GROUP BY");
			appendList(groupBys);
		}
	}

	private void buildOrderByStatement() {
		if (notEmpty(orderBys)) {
			append("ORDER BY");
			appendList(orderBys);
		}
	}

	private String toSql() {
		return sqlBuilder.toString();
	}

	@Override
	public String toString() {
		return build();
	}
}
