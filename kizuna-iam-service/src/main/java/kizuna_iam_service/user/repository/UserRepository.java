package kizuna_iam_service.user.repository;

import kizuna_iam_service.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    List<User> findAllByRolesContaining(String role);
}
