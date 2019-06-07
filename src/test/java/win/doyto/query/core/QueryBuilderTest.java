package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.test.MenuQuery;
import win.doyto.query.core.test.PermissionQuery;
import win.doyto.query.core.test.TestEnum;
import win.doyto.query.core.test.TestQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QueryBuilderTest
 *
 * @author f0rb 2019-05-12
 */
public class QueryBuilderTest {

    private QueryBuilder queryBuilder = new QueryBuilder();
    private List<Object> argList;

    @BeforeEach
    void setUp() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
        argList = new ArrayList<>();
    }

    @Test
    public void buildSelect() {
        TestQuery testQuery = TestQuery.builder().build();
        assertEquals("SELECT * FROM user", queryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    public void buildSelectWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = ?", queryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    public void buildSelectWithWhereAndPage() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        testQuery.setPageNumber(3).setPageSize(10);
        assertEquals("SELECT * FROM user WHERE username = ? LIMIT 10 OFFSET 30",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    public void buildSelectWithCustomWhere() {
        TestQuery testQuery = TestQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    public void buildSelectWithArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT * FROM user WHERE username = ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    public void buildSelectAndArgsWithCustomWhere() {

        TestQuery testQuery = TestQuery.builder().account("test").build();
        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertEquals(3, argList.size());
    }

    @Test
    public void buildCountAndArgsWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        testQuery.setPageNumber(2).setPageSize(10);
        testQuery.setSort("createTime,asc");
        assertEquals("SELECT * FROM user WHERE username = ? ORDER BY createTime asc LIMIT 10 OFFSET 20",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));

        List<Object> countArgList = new ArrayList<>();
        assertEquals("SELECT count(*) FROM user WHERE username = ?",
                     queryBuilder.buildCountAndArgs(testQuery, countArgList));
    }

    @Test
    public void buildCountWithWhere() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        testQuery.setPageNumber(0);
        assertEquals("SELECT count(*) FROM user WHERE username = ?",
                     queryBuilder.buildCountAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test");
    }

    @Test
    public void supportLikeSuffix() {
        TestQuery testQuery = TestQuery.builder().usernameLike("_test%f0rb").build();

        assertEquals("SELECT * FROM user WHERE username LIKE ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("%\\_test\\%f0rb%");
    }

    @Test
    public void supportInSuffix() {
        List<Integer> ids = Arrays.asList(1, 2, 3);
        TestQuery testQuery = TestQuery.builder().idIn(ids).build();

        assertEquals("SELECT * FROM user WHERE id IN (?, ?, ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);

    }

    @Test
    public void supportNotInSuffix() {
        TestQuery testQuery = TestQuery.builder().idNotIn(Arrays.asList(1, 2)).build();

        assertEquals("SELECT * FROM user WHERE id NOT IN (?, ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1, 2);
    }

    @Test
    public void supportGtSuffix() {
        Date createTimeGt = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeGt(createTimeGt).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime > ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", createTimeGt);
    }

    @Test
    public void supportGeSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeGe(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime >= ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    public void supportLtSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime < ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    public void supportLeSuffix() {
        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().username("test").createTimeLe(date).build();

        assertEquals("SELECT * FROM user WHERE username = ? AND createTime <= ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", date);
    }

    @Test
    void supportOr() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobile("test").build();

        assertEquals("SELECT * FROM user WHERE (username = ? OR email = ? OR mobile = ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("test", "test", "test");

    }

    @Test
    void supportOrWithLike() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobileLike("test").build();

        assertEquals("SELECT * FROM user WHERE (username LIKE ? OR email LIKE ? OR mobile LIKE ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("%test%", "%test%", "%test%");

    }

    @Test
    public void supportSort() {
        TestQuery testQuery = TestQuery.builder().usernameLike("test").build();
        testQuery.setPageNumber(5).setPageSize(10).setSort("id,desc;createTime,asc");
        assertEquals("SELECT * FROM user WHERE username LIKE ? ORDER BY id desc, createTime asc LIMIT 10 OFFSET 50",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
    }

    @Test
    public void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        Date date = new Date();
        TestQuery testQuery = TestQuery.builder().userNameOrUserCodeLike("test").createTimeLt(date).build();

        assertEquals("SELECT * FROM user WHERE (user_name LIKE ? OR user_code LIKE ?) AND create_time < ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly("%test%", "%test%", date);
    }

    @Test
    public void buildDeleteAndArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        testQuery.setPageNumber(3).setPageSize(10);
        assertEquals("DELETE FROM user WHERE username = ? LIMIT 10",
                     queryBuilder.buildDeleteAndArgs(testQuery, argList));
        assertEquals(1, argList.size());
        assertEquals("test", argList.get(0));
    }

    @Test
    public void buildSubquery() {
        TestQuery testQuery = TestQuery.builder().roleId(1).build();

        assertEquals("SELECT * FROM user WHERE id IN (SELECT userId FROM t_user_and_role WHERE roleId = ?)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(1);
    }


    @Test
    public void buildNestedQuery() {
        PermissionQuery permissionQuery = PermissionQuery.builder().userId(1).build();

        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                         "(SELECT roleId FROM t_user_and_role WHERE userId = ?))",
                     queryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    public void buildNestedQuery2() {
        MenuQuery menuQuery = MenuQuery.builder().userId(1).build();

        String expected = "SELECT * FROM menu WHERE id IN (" +
            "SELECT menuId FROM t_perm_and_menu pm inner join t_perm p on p.id = pm.perm_id and p.valid = true WHERE permId IN (" +
            "SELECT permId FROM t_role_and_perm rp inner join t_role r on r.id = rp.role_id and r.valid = true WHERE roleId IN (" +
            "SELECT roleId FROM t_user_and_role WHERE userId = ?)))";
        assertEquals(expected, queryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).containsExactly(1);
    }

    @Test
    public void build_boolean_field() {
        MenuQuery menuQuery = MenuQuery.builder().onlyParent(true).build();

        String expected = "SELECT * FROM menu WHERE id IN (SELECT parent_id FROM menu)";
        assertEquals(expected, queryBuilder.buildSelectAndArgs(menuQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    public void buildSelectIdWithArgs() {
        TestQuery testQuery = TestQuery.builder().username("test").build();
        assertEquals("SELECT id FROM user WHERE username = ?",
                     queryBuilder.buildSelectColumnsAndArgs(testQuery, argList, "id"));
        assertEquals(1, argList.size());
        assertThat(argList).containsExactly("test");
    }

    @Test
    public void buildSelectColumnsAndArgs() {
        TestQuery testQuery = TestQuery.builder().build();
        assertEquals("SELECT username, password FROM user",
                     queryBuilder.buildSelectColumnsAndArgs(testQuery, argList, "username", "password"));
    }

    @Test
    public void defaultEnumOrdinal() {
        TestQuery testQuery = TestQuery.builder().userLevel(TestEnum.VIP).build();
        assertEquals("SELECT * FROM user WHERE userLevel = ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(0);

    }

    @Test
    void supportIsNull() {
        MenuQuery byNoParent = MenuQuery.builder().parentIdNull(true).build();

        assertEquals("SELECT * FROM menu WHERE parentId IS NULL",
                     queryBuilder.buildSelectAndArgs(byNoParent, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    void customPageDialect() {
        GlobalConfiguration.instance().setDialect(
            (sql, limit, offset) -> String.format("SELECT LIMIT %d %d %s", offset, offset + limit, sql.substring("SELECT ".length())));

        PageQuery pageQuery = TestQuery.builder().build().setPageNumber(2).setPageSize(10);
        assertEquals("SELECT LIMIT 20 30 * FROM user",
                     queryBuilder.buildSelectAndArgs(pageQuery, argList));

        // reset
        GlobalConfiguration.instance().setDialect(
            (sql, limit, offset) -> sql + " LIMIT " + limit + (sql.startsWith("SELECT") ? " OFFSET " + offset : ""));

    }

    @Test
    public void buildNestedQueryMissLastRightColumn() {
        PermissionQuery permissionQuery = PermissionQuery.builder().validUser(true).build();

        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                         "(SELECT roleId FROM t_user_and_role ur inner join user u on u.id = ur.userId and u.valid = true))",
                     queryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    public void buildSubQueryWithCollection() {
        PermissionQuery permissionQuery = PermissionQuery.builder().roleIdIn(Arrays.asList(1, 2, 3)).build();
        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN (?, ?, ?))",
                     queryBuilder.buildSelectAndArgs(permissionQuery, argList));
        assertThat(argList).containsExactly(1, 2, 3);
    }

    @Test
    public void buildSubQueryWithNullCollection() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        PermissionQuery nullQuery = PermissionQuery.builder().roleIdIn(Arrays.asList()).build();
        assertEquals("SELECT * FROM permission WHERE id IN (SELECT permId FROM t_role_and_perm WHERE role_id IN (null))",
                     queryBuilder.buildSelectAndArgs(nullQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    public void buildSelectColumnsAndArgsWithJoin() {
        TestQuery testQuery = TestQuery.builder().roleId(9).memoNotNull(true).build();
        SqlAndArgs sqlAndArgs = queryBuilder.buildSelectColumnsAndArgsAndJoin(
            testQuery, "LEFT JOIN user_and_role ur ON ur.roleId = #{roleId}", "username", "password");
        assertEquals("SELECT username, password FROM user LEFT JOIN user_and_role ur ON ur.roleId = ? WHERE memo IS NOT NULL",
                     sqlAndArgs.sql);
        assertThat(sqlAndArgs.args).containsExactly(9);
        assertThat(testQuery.getRoleId()).isEqualTo(9);
    }

    @Test
    public void ignoreNotInWhenEmpty() {
        List<Integer> ids = Arrays.asList();
        TestQuery testQuery = TestQuery.builder().idIn(ids).idNotIn(ids).build();

        assertEquals("SELECT * FROM user WHERE id IN (null)",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).isEmpty();
    }

    @Test
    public void supportNot() {
        TestQuery testQuery = TestQuery.builder().userLevelNot(TestEnum.VIP).build();
        assertEquals("SELECT * FROM user WHERE userLevel != ?",
                     queryBuilder.buildSelectAndArgs(testQuery, argList));
        assertThat(argList).containsExactly(0);
    }
}