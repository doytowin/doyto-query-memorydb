/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.mongodb.aggregation;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.mongodb.client.model.Aggregates.*;
import static win.doyto.query.config.GlobalConfiguration.*;
import static win.doyto.query.mongodb.MongoConstant.MONGO_ID;
import static win.doyto.query.mongodb.filter.MongoFilterBuilder.buildFilter;

/**
 * DomainPathBuilder
 *
 * @author f0rb on 2022-05-20
 */
@UtilityClass
public class DomainPathBuilder {
    private static final int PROJECTING = 1;

    public static <V> Bson buildLookUpForSubDomain(DoytoQuery query, Class<V> viewClass, Field field) {
        DomainPath domainPath = field.getAnnotation(DomainPath.class);
        String[] paths = domainPath.value();
        String viewName = field.getName();

        Document projectDoc = new Document();
        ColumnUtil.filterFields(viewClass).forEach(f -> projectDoc.append(f.getName(), PROJECTING));

        if (paths.length == 1) {
            String tableName = String.format(TABLE_FORMAT, paths[0]);
            if (Collection.class.isAssignableFrom(field.getType())) {
                // one-to-many
                return lookup0(tableName, MONGO_ID, domainPath.lastDomainIdColumn(), project(projectDoc), viewName);
            } else {
                // many-to-one
                return lookup0(tableName, domainPath.lastDomainIdColumn(), MONGO_ID, project(projectDoc),viewName);
            }
        }
        return buildLookupForManyToMany(query, field.getName(), paths, projectDoc);
    }

    @SuppressWarnings("java:S117")
    private static Bson buildLookupForManyToMany(DoytoQuery query, String viewName, String[] paths, Document projectDoc) {
        int n = paths.length - 1;
        String $viewName = "$" + viewName;

        boolean needReverse = viewName.contains(paths[0]);
        String[] joints = IntStream.range(0, n).mapToObj(i -> String.format(JOIN_TABLE_FORMAT, paths[i], paths[i + 1]))
                                   .toArray(String[]::new);
        if (needReverse) {
            ArrayUtils.reverse(paths);
            ArrayUtils.reverse(joints);
        }
        String[] tableNames = Arrays.stream(paths).map(path -> String.format(TABLE_FORMAT, path)).toArray(String[]::new);
        String[] joinIds = Arrays.stream(paths).map(path -> String.format(JOIN_ID_FORMAT, path)).toArray(String[]::new);

        List<Bson> pipeline = Arrays.asList(
                lookup0(tableNames[n], joinIds[n], MONGO_ID, Collections.emptyList(), viewName),
                unwind($viewName),
                replaceRoot($viewName),
                match(buildFilter(query)),
                project(projectDoc)
        );

        for (int i = n - 1; i > 0; i--) {
            pipeline = Arrays.asList(
                    lookup0(joints[i], joinIds[i], joinIds[i], pipeline, viewName),
                    unwind($viewName),
                    replaceRoot($viewName)
            );
        }

        return lookup0(joints[0], MONGO_ID, joinIds[0], pipeline, viewName);
    }

    private static Bson lookup0(String from, String localField, String foreignField, Bson project, String as) {
        return lookup0(from, localField, foreignField, Collections.singletonList(project), as);
    }

    public static Bson lookup0(
            String from, String localField, String foreignField, List<? extends Bson> pipeline, String as
    ) {
        Document lookupDoc = new Document()
                .append("from", from)
                .append("localField", localField)
                .append("foreignField", foreignField);
        if (!pipeline.isEmpty()) {
            lookupDoc.append("pipeline", pipeline);
        }
        lookupDoc.append("as", as);
        return new Document("$lookup", lookupDoc);
    }

    @SuppressWarnings("java:S117")
    public static Bson buildLookUpForNestedQuery(String viewName, DomainPath domainPath) {
        String[] paths = domainPath.value();
        if (paths.length == 1) {
            String tableName = String.format(TABLE_FORMAT, paths[0]);
            // one-to-many
            return lookup0(tableName, domainPath.localField(), MONGO_ID, Collections.emptyList(), viewName);
        }

        int n = paths.length - 1;
        String $viewName = "$" + viewName;

        boolean needReverse = viewName.contains(paths[0]);
        String[] joints = IntStream.range(0, n).mapToObj(i -> String.format(JOIN_TABLE_FORMAT, paths[i], paths[i + 1]))
                                   .toArray(String[]::new);
        if (needReverse) {
            ArrayUtils.reverse(paths);
            ArrayUtils.reverse(joints);
        }
        String[] tableNames = Arrays.stream(paths).map(path -> String.format(TABLE_FORMAT, path)).toArray(String[]::new);
        String[] joinIds = Arrays.stream(paths).map(path -> String.format(JOIN_ID_FORMAT, path)).toArray(String[]::new);

        List<Bson> pipeline = Arrays.asList(
                lookup0(tableNames[n], joinIds[n], MONGO_ID, Collections.emptyList(), viewName),
                replaceRoot(new Document("$arrayElemAt", Arrays.asList($viewName, 0)))
        );

        for (int i = n - 1; i > 0; i--) {
            pipeline = Arrays.asList(
                    lookup0(joints[i], joinIds[i], joinIds[i], pipeline, viewName),
                    replaceRoot(new Document("$arrayElemAt", Arrays.asList($viewName, 0)))
            );
        }

        return lookup0(joints[0], MONGO_ID, joinIds[0], pipeline, viewName);
    }
}