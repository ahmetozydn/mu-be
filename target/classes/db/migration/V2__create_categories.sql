CREATE TABLE categories (
    id             VARCHAR(50) PRIMARY KEY,
    type           VARCHAR(20) NOT NULL,
    slug_tr        VARCHAR(100) NOT NULL UNIQUE,
    slug_en        VARCHAR(100) NOT NULL UNIQUE,
    title_tr       VARCHAR(100) NOT NULL,
    title_en       VARCHAR(100) NOT NULL,
    description_tr TEXT,
    description_en TEXT,
    icon           VARCHAR(50),
    sort_order     INT NOT NULL DEFAULT 0,
    active         BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_categories_active ON categories(active);
CREATE INDEX idx_categories_sort_order ON categories(sort_order);
