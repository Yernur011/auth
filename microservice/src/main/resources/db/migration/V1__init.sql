CREATE TABLE users
(
    id            UUID         NOT NULL,
    username      VARCHAR(64)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE user_roles
(
    user_id UUID        NOT NULL,
    role    VARCHAR(32) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE tokens
(
    id         UUID         NOT NULL,
    value      VARCHAR(512) NOT NULL,
    user_id    UUID         NOT NULL,
    type       VARCHAR(16)  NOT NULL,
    issued_at  TIMESTAMP    NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_tokens PRIMARY KEY (id),
    CONSTRAINT uq_tokens_value UNIQUE (value)
);

CREATE INDEX idx_tokens_value ON tokens (value);
CREATE INDEX idx_tokens_user_id ON tokens (user_id);
