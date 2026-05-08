CREATE DATABASE IF NOT EXISTS szavazzapp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_hungarian_ci;

USE szavazzapp;

DROP TABLE IF EXISTS poll_topics;
DROP TABLE IF EXISTS poll_options;
DROP TABLE IF EXISTS polls;
DROP TABLE IF EXISTS topics;
DROP TABLE IF EXISTS user_accounts;

CREATE TABLE user_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_accounts_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_hungarian_ci;

CREATE TABLE topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_topics_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_hungarian_ci;

CREATE TABLE polls (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_polls_owner_id (owner_id),
    CONSTRAINT fk_polls_owner
        FOREIGN KEY (owner_id)
        REFERENCES user_accounts (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_hungarian_ci;

CREATE TABLE poll_topics (
    poll_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    PRIMARY KEY (poll_id, topic_id),
    KEY ix_poll_topics_topic_id (topic_id),
    CONSTRAINT fk_poll_topics_poll
        FOREIGN KEY (poll_id)
        REFERENCES polls (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_poll_topics_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics (id)
        ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_hungarian_ci;

CREATE TABLE poll_options (
    id BIGINT NOT NULL AUTO_INCREMENT,
    poll_id BIGINT NOT NULL,
    label VARCHAR(180) NOT NULL,
    vote_count INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY ix_poll_options_poll_id (poll_id),
    CONSTRAINT fk_poll_options_poll
        FOREIGN KEY (poll_id)
        REFERENCES polls (id)
        ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_hungarian_ci;

INSERT INTO user_accounts (id, username, display_name) VALUES
    (1, 'user', 'Teszt Felhasználó'),
    (2, 'admin', 'Adminisztrátor'),
    (3, 'anna', 'Anna'),
    (4, 'peter', 'Péter'),
    (5, 'eszter', 'Eszter'),
    (6, 'system', 'Rendszer');

INSERT INTO topics (id, name) VALUES
    (1, 'Technológia'),
    (2, 'Közösség'),
    (3, 'Oktatás'),
    (4, 'Fejlesztés'),
    (5, 'Backend'),
    (6, 'Frontend'),
    (7, 'Vélemény'),
    (8, 'Rendszer'),
    (9, 'Szabályok'),
    (10, 'Teszt'),
    (11, 'Saját');

INSERT INTO polls (id, title, description, status, owner_id, created_at) VALUES
    (1, 'Top 1 placeholder', 'Top desc', 'ACTIVE', 6, NOW()),
    (2, 'Top 2 placeholder', 'Top desc', 'ACTIVE', 6, NOW()),
    (101, 'Top 3 placeholder', 'Top desc', 'ACTIVE', 3, NOW()),
    (102, 'Top 4 placeholder', 'Top desc', 'ACTIVE', 4, NOW()),
    (103, 'Top 5 placeholder', 'Top desc', 'ACTIVE', 5, NOW()),
    (201, 'Top 6 placeholder', 'Top desc', 'ACTIVE', 1, NOW()),
    (202, 'Top 7 placeholder', 'Top desc', 'ACTIVE', 1, NOW());

INSERT INTO poll_topics (poll_id, topic_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (101, 4),
    (101, 5),
    (101, 6),
    (102, 2),
    (102, 7),
    (103, 8),
    (103, 9),
    (201, 10),
    (201, 11),
    (202, 4),
    (202, 11);

INSERT INTO poll_options (poll_id, label, vote_count, sort_order) VALUES
    (1, 'Igen', 72, 1),
    (1, 'Nem', 31, 2),
    (1, 'Talán', 25, 3),
    (2, 'Hasznos', 77, 1),
    (2, 'Nem hasznos', 17, 2),
    (101, 'Admin felület', 9, 1),
    (101, 'Szavazás létrehozása', 14, 2),
    (101, 'Eredménydiagramok', 8, 3),
    (101, 'Felhasználói profil', 4, 4),
    (102, 'Technológia', 7, 1),
    (102, 'Sport', 2, 2),
    (102, 'Oktatás', 6, 3),
    (102, 'Szórakozás', 3, 4),
    (103, 'Igen, mindig', 19, 1),
    (103, 'Nem szükséges', 11, 2),
    (103, 'Legyen opcionális', 21, 3),
    (201, 'Első opció', 2, 1),
    (201, 'Második opció', 4, 2),
    (201, 'Harmadik opció', 1, 3),
    (202, 'Megjelenítés', 1, 1),
    (202, 'Szerkesztés', 1, 2),
    (202, 'Törlés', 1, 3);