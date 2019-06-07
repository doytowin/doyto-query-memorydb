package win.doyto.query.core.test;

import lombok.*;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.QueryField;
import win.doyto.query.core.QueryTable;
import win.doyto.query.core.SubQuery;

import java.util.Date;
import java.util.List;

/**
 * TestQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@QueryTable(table = TestEntity.TABLE)
public class TestQuery extends PageQuery {
    private List<Integer> idIn;
    private List<Integer> idNotIn;
    private Integer idLt;
    private Integer idLe;

    private String username;

    @SubQuery(left = "userId", table = "t_user_and_role")
    private Integer roleId;

    @QueryField(and = "(username = ? OR email = ? OR mobile = ?)")
    private String account;

    private String usernameOrEmailOrMobile;

    private String usernameOrEmailOrMobileLike;

    private String usernameLike;

    private String userNameOrUserCodeLike;

    private boolean memoNull;
    private boolean memoNotNull;

    private TestEnum userLevel;

    private TestEnum userLevelNot;

    private Date createTimeGt;
    private Date createTimeGe;
    private Date createTimeLt;
    private Date createTimeLe;

    private Boolean valid;

}
