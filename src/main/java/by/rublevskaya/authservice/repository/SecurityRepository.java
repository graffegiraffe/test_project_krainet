package by.rublevskaya.authservice.repository;

import by.rublevskaya.authservice.dto.UserResponse;
import by.rublevskaya.authservice.model.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {
    Security findByLogin(String login);
    @Query("SELECT new by.rublevskaya.authservice.dto.UserResponse( " +
            "s.login, u.email, u.firstName, u.lastName) " +
            "FROM Security s " +
            "JOIN User u ON s.userId = u.id " +
            "WHERE s.id = :id")
    Optional<UserResponse> findUserDetailsBySecurityId(@Param("id") Long id);
}