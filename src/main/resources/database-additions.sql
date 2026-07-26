CREATE TABLE IF NOT EXISTS saved_routes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_saved_route_user_route UNIQUE (user_id, route_id),
    CONSTRAINT fk_saved_routes_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_saved_routes_route FOREIGN KEY (route_id) REFERENCES routes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(1000) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_reviews_route_status_active (route_id, status, active),
    INDEX idx_reviews_user_active (user_id, active),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reviews_route FOREIGN KEY (route_id) REFERENCES routes (id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS support_ticket (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    issue_type ENUM('FARE_DISPUTE','INCORRECT_ROUTE','INCORRECT_TAXI_RANK','SAFETY_CONCERN','ACCOUNT_PROBLEM','TECHNICAL_PROBLEM','OTHER') NOT NULL,
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    priority ENUM('LOW','MEDIUM','HIGH','URGENT') NOT NULL DEFAULT 'MEDIUM',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_support_tickets_user_created (user_id, created_at),
    INDEX idx_support_tickets_status_priority (status, priority),
    CONSTRAINT fk_support_tickets_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
