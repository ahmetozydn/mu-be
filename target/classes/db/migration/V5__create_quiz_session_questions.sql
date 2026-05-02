CREATE TABLE quiz_session_questions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  UUID NOT NULL REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    position    SMALLINT NOT NULL,
    UNIQUE (session_id, position)
);

CREATE INDEX idx_quiz_session_questions_session_id ON quiz_session_questions(session_id);
