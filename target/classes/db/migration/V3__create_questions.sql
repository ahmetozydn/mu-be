CREATE TABLE questions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id   VARCHAR(50) NOT NULL REFERENCES categories(id),
    language      VARCHAR(5) NOT NULL,
    difficulty    VARCHAR(10) NOT NULL,
    question_text TEXT NOT NULL,
    explanation   TEXT,
    correct_index SMALLINT NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE question_options (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id  UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_index SMALLINT NOT NULL,
    option_text  TEXT NOT NULL,
    UNIQUE (question_id, option_index)
);

CREATE INDEX idx_questions_category_id ON questions(category_id);
CREATE INDEX idx_questions_language ON questions(language);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_questions_active ON questions(active);
CREATE INDEX idx_question_options_question_id ON question_options(question_id);
