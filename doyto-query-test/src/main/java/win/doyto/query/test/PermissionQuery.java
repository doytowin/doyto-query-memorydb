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

package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.core.PageQuery;

/**
 * PermissionQuery
 *
 * @author f0rb on 2019-05-28
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionQuery extends PageQuery {

    private DoytoDomainRoute domainRoute;

    @NestedQueries(value = {
            @NestedQuery(select = "permId", from = "t_role_and_perm"),
            @NestedQuery(select = "roleId", from = "t_user_and_role ur",
                    extra = "inner join user u on u.id = ur.userId and u.valid = ?"
            )
    }, appendWhere = false)
    private Boolean validUser;

    @NestedQueries(value = {
            @NestedQuery(select = "permId", from = "t_role_and_perm"),
            @NestedQuery(select = "roleId", from = "t_user_and_role", where = "userId"),
            @NestedQuery(select = "id", from = "t_user")
    })
    private UserQuery user;

    @NestedQueries(value = {
            @NestedQuery(select = "permId", from = "t_role_and_perm", where = "roleId"),
            @NestedQuery(select = "id", from = "t_role")
    })
    private String roleCodeLikeOrRoleNameLike;

}
