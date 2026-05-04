CREATE TABLE event_log (
    id                BIGSERIAL PRIMARY KEY,
    client_event_id   VARCHAR(64)  NOT NULL UNIQUE,
    user_id           BIGINT       NOT NULL,
    event_type        VARCHAR(32)  NOT NULL,
    visit_session_id  VARCHAR(64)  NOT NULL,
    session_id        BIGINT,
    properties        JSONB,
    created_at        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_event_log_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_event_log_session
        FOREIGN KEY (session_id) REFERENCES correction_session (id)
);

CREATE INDEX idx_event_log_user ON event_log (user_id);
CREATE INDEX idx_event_log_session ON event_log (session_id);
CREATE INDEX idx_event_log_type ON event_log (event_type);
