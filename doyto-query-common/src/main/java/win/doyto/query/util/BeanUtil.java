package win.doyto.query.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import win.doyto.query.geo.Point;
import win.doyto.query.geo.PointDeserializer;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * BeanUtil
 *
 * @author f0rb
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtil {

    private static final ObjectMapper objectMapper;
    private static final ObjectMapper objectMapper2;

    static {
        objectMapper = new ObjectMapper()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper2 = objectMapper
                .copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        BeanUtil.register(Point.class, new PointDeserializer());

    }

    public static Type[] getActualTypeArguments(Class<?> clazz) {
        Type genericSuperclass = clazz;
        do {
            genericSuperclass = ((Class<?>) genericSuperclass).getGenericSuperclass();
        } while (!(genericSuperclass instanceof ParameterizedType));
        return ((ParameterizedType) genericSuperclass).getActualTypeArguments();
    }

    public static <T> T loadJsonData(String path, TypeReference<T> typeReference) throws IOException {
        return loadJsonData(typeReference.getClass().getResourceAsStream(path), typeReference);
    }

    public static <T> T loadJsonData(InputStream resourceAsStream, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(resourceAsStream, typeReference);
    }

    @SneakyThrows
    public static String stringify(Object target) {
        return objectMapper2.writeValueAsString(target);
    }

    @SneakyThrows
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }

    @SneakyThrows
    public static <T> T parse(String json, Class<T> clazz) {
        return objectMapper.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T convertTo(Object source, TypeReference<T> typeReference) {
        return objectMapper.readValue(objectMapper.writeValueAsBytes(source), typeReference);
    }

    @SneakyThrows
    public static <T> T convertTo(Object source, Class<T> targetType) {
        return objectMapper.readValue(objectMapper.writeValueAsBytes(source), targetType);
    }

    @SneakyThrows
    public static <T> T convertToIgnoreNull(Object source, TypeReference<T> typeReference) {
        return objectMapper2.readValue(objectMapper2.writeValueAsBytes(source), typeReference);
    }

    @SneakyThrows
    public static <T> T convertToIgnoreNull(Object source, Class<T> targetType) {
        return objectMapper2.readValue(objectMapper2.writeValueAsBytes(source), targetType);
    }

    @SneakyThrows
    public static <T> T copyTo(Object from, T to) {
        return objectMapper.updateValue(to, from);
    }

    @SneakyThrows
    public static <T> T copyNonNull(Object from, T to) {
        return objectMapper2.updateValue(to, from);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <I> Class<I> getIdClass(Class<?> entityClass) {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor("id", entityClass);
        return (Class<I>) propertyDescriptor.getPropertyType();
    }

    public static <T> void register(Class<T> clazz, JsonDeserializer<T> jsonDeserializer) {
        SimpleModule mod = new SimpleModule(clazz.getName(), new Version(1, 0, 0, "", "win.doyto", "doyto-query-code"));
        mod.addDeserializer(clazz, jsonDeserializer);
        objectMapper.registerModule(mod);
        objectMapper2.registerModule(mod);
    }
}