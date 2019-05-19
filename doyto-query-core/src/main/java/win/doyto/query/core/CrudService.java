package win.doyto.query.core;

import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CrudService
 *
 * @author f0rb
 * @date 2019-05-19
 */
public interface CrudService<E extends Persistable<I>, I extends Serializable, Q> extends QueryService<E, Q> {

    E get(I id);

    default void create(E e) {
        save(e);
    }

    default void update(E e) {
        save(e);
    }

    E fetch(I id);

    E save(E e);

    @Transactional
    default List<E> save(Iterable<E> entities) {
        List<E> result = new ArrayList<>();
        if (entities == null) {
            return result;
        } else {
            for (E entity : entities) {
                result.add(this.save(entity));
            }
            return result;
        }
    }

    E delete(I id);
}