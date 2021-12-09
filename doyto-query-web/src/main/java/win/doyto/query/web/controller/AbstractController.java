package win.doyto.query.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.DynamicService;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.component.ListValidator;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * AbstractController
 *
 * @author f0rb on 2020-05-05
 */
@JsonBody
abstract class AbstractController<
        E extends Persistable<I>,
        I extends Serializable,
        Q extends DoytoQuery,
        R, S,
        W extends IdWrapper<I>,
        C extends DynamicService<E, I, Q>
        > {

    @Resource
    protected ListValidator listValidator = new ListValidator();

    private final Class<E> entityClass;
    private final TypeReference<W> typeReference;
    protected C service;
    protected Function<E, S> e2rspTransfer;
    protected Function<R, E> req2eTransfer;

    @SuppressWarnings("unchecked")
    protected AbstractController(C service, TypeReference<W> typeReference) {
        this.service = service;
        this.typeReference = typeReference;
        Type[] types = BeanUtil.getActualTypeArguments(getClass());
        this.entityClass = (Class<E>) types[0];

        req2eTransfer = r -> (E) r;
        e2rspTransfer = e -> (S) e;
        if (types.length > 4) {
            if (!entityClass.equals(types[3])) {
                req2eTransfer = r -> BeanUtil.convertTo(r, entityClass);
            }
            if (!entityClass.equals(types[4])) {
                Class<S> responseClass = (Class<S>) types[4];
                e2rspTransfer = e -> BeanUtil.convertTo(e, responseClass);
            }
        }
    }

    protected S buildResponse(E e) {
        return e2rspTransfer.apply(e);
    }

    protected E buildEntity(R r) {
        return req2eTransfer.apply(r);
    }

    protected void checkResult(E e) {
        ErrorCode.assertNotNull(e, PresetErrorCode.ENTITY_NOT_FOUND);
    }

    public PageList<S> page(Q q) {
        q.forcePaging();
        return new PageList<>(this.query(q), service.count(q));
    }

    public List<S> query(Q q) {
        return service.query(q, this::buildResponse);
    }

    public long count(Q q) {
        return service.count(q);
    }

    public void patch(R request) {
        E e = buildEntity(request);
        int count = service.patch(e);
        ErrorCode.assertTrue(count == 1, PresetErrorCode.ENTITY_NOT_FOUND);
    }

    public void update(R request) {
        W w = BeanUtil.convertTo(request, typeReference);
        E e = service.get(w);
        checkResult(e);
        BeanUtil.copyTo(request, e).setId(w.getId());
        service.update(e);
    }

    public void create(List<R> requests) {
        listValidator.validateList(requests);
        if (requests.size() == 1) {
            service.create(buildEntity(requests.get(0)));
        } else {
            service.create(requests.stream().map(this::buildEntity).collect(Collectors.toList()));
        }
    }

    @Resource
    public void setBeanFactory(AutowireCapableBeanFactory beanFactory) throws BeansException {
        if (service.getClass().isAnonymousClass()) {
            beanFactory.autowireBean(service);
        }
    }
}
