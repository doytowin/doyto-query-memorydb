package win.doyto.query.module.user;

import org.springframework.data.repository.CrudRepository;

/**
 * UserRepository
 *
 * @author f0rb
 * @date 2019-05-12
 */
interface UserRepository extends CrudRepository<UserEntity, Long> {
}