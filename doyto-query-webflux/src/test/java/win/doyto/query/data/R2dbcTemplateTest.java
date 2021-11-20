package win.doyto.query.data;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import win.doyto.query.core.SqlAndArgs;

/**
 * R2dbcTemplateTest
 *
 * @author f0rb on 2021-11-20
 */
class R2dbcTemplateTest {

    R2dbcTemplate r2dbc;

    @BeforeEach
    void setUp() {
        ConnectionFactory connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///testdb");
        Flux.from(connectionFactory.create())
            .flatMap(
                    c -> Flux.from(
                            c.createBatch()
                             .add("DROP TABLE IF EXISTS t_role;")
                             .add("CREATE TABLE t_role\n" +
                                          "(\n" +
                                          "    id        int generated by default as identity (start with 1) primary key,\n" +
                                          "    role_name VARCHAR(100) not null,\n" +
                                          "    role_code VARCHAR(100) not null,\n" +
                                          "    valid     boolean DEFAULT TRUE\n" +
                                          ");")
                             .add("INSERT INTO t_role (role_name, role_code) VALUES ('admin', 'ADMIN');")
                             .add("INSERT INTO t_role (role_name, role_code) VALUES ('vip', 'VIP');")
                             .add("INSERT INTO t_role (role_name, role_code) VALUES ('vip2', 'VIP2');")
                             .execute()
                    ).doFinally((a) -> c.close())
            )
            .log()
            .blockLast();

        r2dbc = new R2dbcTemplate(connectionFactory);
    }

    @Test
    void count() {
        r2dbc.count(new SqlAndArgs("SELECT count(*) FROM t_role"))
             .as(StepVerifier::create)
             .expectNext(3L)
             .verifyComplete();
    }

    @Test
    void countWithArgs() {
        r2dbc.count(new SqlAndArgs("SELECT count(*) FROM t_role WHERE role_name LIKE ?", "%vip%"))
             .as(StepVerifier::create)
             .expectNext(2L)
             .verifyComplete();
    }

    @Test
    void insert() {
        String sql = "INSERT INTO t_role (role_name, role_code, valid) VALUES (?, ?, ?)";
        Object[] args = new Object[]{"高级3", "VIP3", true};

        r2dbc.insert(new SqlAndArgs(sql, args), "id", Integer.class)
             .as(StepVerifier::create)
             .expectNext(4)
             .verifyComplete();

        String countSql = "SELECT count(*) FROM t_role WHERE role_code LIKE ?";
        r2dbc.count(new SqlAndArgs(countSql, "VIP%"))
             .as(StepVerifier::create)
             .expectNext(3L)
             .verifyComplete();
    }
}
