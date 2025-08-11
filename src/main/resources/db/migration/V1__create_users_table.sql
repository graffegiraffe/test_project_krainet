CREATE TABLE auth_service.users (
                                    id BIGSERIAL PRIMARY KEY,
                                    username VARCHAR(50) NOT NULL UNIQUE,
                                    password VARCHAR(100) NOT NULL,
                                    email VARCHAR(100) NOT NULL UNIQUE,
                                    first_name VARCHAR(100),
                                    last_name VARCHAR(100),
                                    role VARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
                                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);