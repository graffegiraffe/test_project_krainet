package by.rublevskaya.authservice.repository;

import by.rublevskaya.authservice.model.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {
    Security findByLogin(String login);
}