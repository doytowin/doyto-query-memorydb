package win.doyto.query.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static win.doyto.query.core.CommonUtil.isValidValue;
import static win.doyto.query.core.CommonUtil.readFieldGetter;
import static win.doyto.query.core.Constant.*;

/**
 * BuildHelper
 *
 * @author f0rb on 2021-02-16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildHelper {
    private static final Pattern PTN_SORT = Pattern.compile(",(asc|desc)", Pattern.CASE_INSENSITIVE);
    private static final Map<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    static String buildStart(String[] columns, String from) {
        return Constant.SELECT + StringUtils.join(columns, SEPARATOR) + FROM + from;
    }

    public static String buildWhere(String sql, PageQuery query, List<Object> argList) {
        initFields(query);
        Field[] fields = classFieldsMap.get(query.getClass());
        StringJoiner whereList = new StringJoiner(" AND ", fields.length);
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String and = FieldProcessor.execute(field, argList, value);
                if (and != null) {
                    whereList.append(and);
                }
            }
        }
        if (!whereList.isEmpty()) {
            sql += WHERE + whereList.toString();
        }
        return sql;
    }

    private static void initFields(Object query) {
        Class<?> clazz = query.getClass();
        if (!classFieldsMap.containsKey(clazz)) {
            classFieldsMap.put(clazz, Arrays.stream(clazz.getDeclaredFields()).filter(CommonUtil::fieldFilter).toArray(Field[]::new));
            for (Field field : classFieldsMap.get(clazz)) {
                FieldProcessor.init(field);
            }
        }
    }

    static String buildOrderBy(String sql, PageQuery pageQuery) {
        if (pageQuery.getSort() != null) {
            sql += " ORDER BY " + PTN_SORT.matcher(pageQuery.getSort()).replaceAll(" $1").replace(";", SEPARATOR);
        }
        return sql;
    }

    static String buildPaging(String sql, PageQuery pageQuery) {
        if (pageQuery.needPaging()) {
            sql = GlobalConfiguration.dialect().buildPageSql(sql, pageQuery.getPageSize(), pageQuery.calcOffset());
        }
        return sql;
    }

}
