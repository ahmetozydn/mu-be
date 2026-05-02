CREATE TABLE quiz_answers (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id     UUID NOT NULL REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    question_id    UUID NOT NULL REFERENCES questions(id),
    position       SMALLINT NOT NULL,
    selected_index SMALLINT NOT NULL,
    is_correct     BOOLEAN NOT NULL,
    answered_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_answers_session_id ON quiz_answers(session_id);
