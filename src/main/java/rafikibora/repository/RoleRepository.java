package rafikibora.repository;

import rafikibora.model.users.Roles;
import rafikibora.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles, Long>{
    Optional<Roles> findByRoleName(String name);
}
