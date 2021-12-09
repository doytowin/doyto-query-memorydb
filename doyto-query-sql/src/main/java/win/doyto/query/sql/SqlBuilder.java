package win.doyto.query.sql;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;

/**
 * SqlBuilder
 *
 * @author f0rb on 2021-11-21
 */
public interface SqlBuilder<E extends Persistable<?>> {

    SqlAndArgs buildCountAndArgs(DoytoQuery query);

    SqlAndArgs buildSelectColumnsAndArgs(DoytoQuery query, String... columns);

    SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns);

    SqlAndArgs buildSelectIdAndArgs(DoytoQuery query);

    SqlAndArgs buildCreateAndArgs(E testEntity);

    SqlAndArgs buildCreateAndArgs(Iterable<E> entities, String... columns);

    SqlAndArgs buildUpdateAndArgs(E entity);

    SqlAndArgs buildPatchAndArgsWithId(E entity);

    SqlAndArgs buildPatchAndArgsWithQuery(E entity, DoytoQuery query);

    SqlAndArgs buildDeleteAndArgs(DoytoQuery query);

    SqlAndArgs buildDeleteById(IdWrapper<?> w);

}