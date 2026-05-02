CREATE TABLE quiz_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    category_id     VARCHAR(50) REFERENCES categories(id),
    language        VARCHAR(5) NOT NULL,
    difficulty      VARCHAR(10),
    is_karma        BOOLEAN NOT NULL DEFAULT FALSE,
    is_guest        BOOLEAN NOT NULL DEFAULT FALSE,
    current_index   SMALLINT NOT NULL DEFAULT 0,
    score           SMALLINT,
    total_questions SMALLINT NOT NULL,
    duration_sec    INT,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP WITH TIME ZONE,
    expires_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_quiz_sessions_user_id ON quiz_sessions(user_id);
CREATE INDEX idx_quiz_sessions_status ON quiz_sessions(status);
CREATE INDEX idx_quiz_sessions_expires_at ON quiz_sessions(expires_at);
