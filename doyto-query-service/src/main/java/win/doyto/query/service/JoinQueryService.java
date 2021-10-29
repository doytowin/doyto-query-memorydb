package win.doyto.query.service;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import win.doyto.query.core.JoinQueryBuilder;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.SqlAndArgs;
import win.doyto.query.data.DatabaseOperations;

import java.util.List;

/**
 * JoinQueryService
 *
 * @author f0rb on 2019-06-09
 */
@AllArgsConstructor
public class JoinQueryService<E, Q extends PageQuery> implements QueryService<E, Q> {

    private DatabaseOperations databaseOperations;
    private final JoinQueryBuilder joinQueryBuilder;
    private final BeanPropertyRowMapper<E> beanPropertyRowMapper;

    public JoinQueryService(Class<E> entityClass) {
        this.joinQueryBuilder = new JoinQueryBuilder(entityClass);
        this.beanPropertyRowMapper = new BeanPropertyRowMapper<>(entityClass);
    }

    public JoinQueryService(DatabaseOperations databaseOperations, Class<E> entityClass) {
        this(entityClass);
        this.databaseOperations = databaseOperations;
    }

    public List<E> query(Q q) {
        SqlAndArgs sqlAndArgs = buildJoinSelectAndArgs(q);
        return databaseOperations.query(sqlAndArgs, beanPropertyRowMapper);
    }

    public long count(Q q) {
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinCountAndArgs(q);
        return databaseOperations.count(sqlAndArgs);
    }

    public SqlAndArgs buildJoinSelectAndArgs(Q q) {
        return joinQueryBuilder.buildJoinSelectAndArgs(q);
    }
}