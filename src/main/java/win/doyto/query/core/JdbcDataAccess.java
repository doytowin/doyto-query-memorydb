package win.doyto.query.core;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Table;

import static win.doyto.query.core.CommonUtil.replaceTableName;

/**
 * JdbcDataAccess
 *
 * @author f0rb
 */
class JdbcDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<E> rowMapper;
    private final String getById;
    private final String deleteById;
    private final CrudBuilder<E> crudBuilder;
    private static final String FMT_GET_BY_ID = "SELECT * FROM %s WHERE id = ?";
    private static final String FMT_DELETE_BY_ID = "DELETE FROM %s WHERE id = ?";
    private final String entityTable;

    public JdbcDataAccess(JdbcTemplate jdbcTemplate, Class<E> entityClass) {
        this.jdbcTemplate = jdbcTemplate;

        rowMapper = new BeanPropertyRowMapper<>(entityClass);

        entityTable = entityClass.getAnnotation(Table.class).name();
        getById = String.format(FMT_GET_BY_ID, entityTable);
        deleteById = String.format(FMT_DELETE_BY_ID, entityTable);
        crudBuilder = new CrudBuilder<>(entityClass);
    }

    @Override
    public List<E> query(Q q) {
        return queryColumns(q, rowMapper, "*");
    }

    @Override
    public <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildSelectColumnsAndArgs(q, args, columns);
        return jdbcTemplate.query(sql, args.toArray(), rowMapper);
    }

    @Override
    public long count(Q q) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildCountAndArgs(q, args);
        return jdbcTemplate.queryForObject(sql, args.toArray(), Long.class);
    }

    @Override
    public int delete(Q q) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildDeleteAndArgs(q, args);
        return jdbcTemplate.update(sql, args.toArray(), rowMapper);
    }

    @Override
    public E get(I id) {
        return getEntity(getById, id);
    }

    @Override
    public int delete(I id) {
        return jdbcTemplate.update(deleteById, id);
    }

    @Override
    public E get(E e) {
        String sql = String.format(FMT_GET_BY_ID, replaceTableName(e, entityTable));
        return getEntity(sql, e.getId());
    }

    private E getEntity(String sql, I id) {
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public int delete(E e) {
        return jdbcTemplate.update(String.format(FMT_DELETE_BY_ID, replaceTableName(e, entityTable)), e.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void create(E e) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildCreateAndArgs(e, args);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object arg : args) {
                ps.setObject(i++, arg);
            }
            return ps;
        }, keyHolder);
        e.setId((I) keyHolder.getKey());
    }

    @Override
    public void update(E e) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildUpdateAndArgs(e, args);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void patch(E e) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildPatchAndArgsWithId(e, args);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void patch(E e, Q q) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildPatchAndArgsWithQuery(e, q, args);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public E fetch(I id) {
        return get(id);
    }

}
