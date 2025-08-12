CREATE TABLE IF NOT EXISTS auth_service.security (
                                                     id BIGSERIAL
                                                         PRIMARY KEY,
                                                     login VARCHAR(20) NOT NULL UNIQUE,
                                                     password VARCHAR(20) NOT NULL,
                                                     role VARCHAR(20) DEFAULT 'USER' NOT NULL,
                                                     created TIMESTAMP DEFAULT NOW() NOT NULL,
                                                     updated TIMESTAMP DEFAULT NOW(),
                                                     user_id BIGINT NOT NULL,
                                                     CONSTRAINT fk_security_user FOREIGN KEY (user_id) REFERENCES auth_service.users (id) ON DELETE CASCADE
);

