#!/usr/bin/env python3

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "src" / "main" / "resources" / "data"
MIGRATION_DIR = ROOT / "src" / "main" / "resources" / "db" / "migration"

CATEGORY_MIGRATION = MIGRATION_DIR / "V13__seed_extra_categories.sql"
QUESTION_MIGRATION = MIGRATION_DIR / "V14__seed_extra_questions_tr.sql"

CATEGORY_DEFS = [
    {
        "id": "mobile",
        "type": "POSITION",
        "slug_tr": "mobile-developer-mulakat-sorulari",
        "slug_en": "mobile-interview-questions",
        "title_tr": "Mobile Developer",
        "title_en": "Mobile Developer",
        "description_tr": "Mobil uygulama geliştirme, mimari, performans ve platform temelleri",
        "description_en": "Mobile app development, architecture, performance, and platform fundamentals",
        "icon": "Smartphone",
        "sort_order": 14,
    },
    {
        "id": "testing",
        "type": "TECHNICAL",
        "slug_tr": "testing-mulakat-sorulari",
        "slug_en": "testing-interview-questions",
        "title_tr": "Testing",
        "title_en": "Testing",
        "description_tr": "Yazilim testi, otomasyon, kalite guvencesi ve test stratejileri",
        "description_en": "Software testing, automation, QA, and test strategy",
        "icon": "TestTube",
        "sort_order": 15,
    },
    {
        "id": "linux",
        "type": "TECHNICAL",
        "slug_tr": "linux-mulakat-sorulari",
        "slug_en": "linux-interview-questions",
        "title_tr": "Linux",
        "title_en": "Linux",
        "description_tr": "Linux komutlari, sistem yonetimi, prosesler ve dosya sistemi",
        "description_en": "Linux commands, system administration, processes, and file systems",
        "icon": "Terminal",
        "sort_order": 16,
    },
    {
        "id": "git",
        "type": "TECHNICAL",
        "slug_tr": "git-mulakat-sorulari",
        "slug_en": "git-interview-questions",
        "title_tr": "Git",
        "title_en": "Git",
        "description_tr": "Versiyon kontrolu, branching stratejileri ve Git is akislari",
        "description_en": "Version control, branching strategies, and Git workflows",
        "icon": "GitBranch",
        "sort_order": 17,
    },
    {
        "id": "kotlin",
        "type": "LANGUAGE",
        "slug_tr": "kotlin-mulakat-sorulari",
        "slug_en": "kotlin-interview-questions",
        "title_tr": "Kotlin Developer",
        "title_en": "Kotlin Developer",
        "description_tr": "Kotlin dili, coroutine, null safety ve modern JVM gelistirme",
        "description_en": "Kotlin language, coroutines, null safety, and modern JVM development",
        "icon": "Code2",
        "sort_order": 18,
    },
    {
        "id": "swift",
        "type": "LANGUAGE",
        "slug_tr": "swift-mulakat-sorulari",
        "slug_en": "swift-interview-questions",
        "title_tr": "Swift Developer",
        "title_en": "Swift Developer",
        "description_tr": "Swift dili, ARC, protocol, concurrency ve iOS odakli gelistirme",
        "description_en": "Swift language, ARC, protocols, concurrency, and iOS-focused development",
        "icon": "Code",
        "sort_order": 19,
    },
    {
        "id": "data-engineer",
        "type": "POSITION",
        "slug_tr": "data-engineer-mulakat-sorulari",
        "slug_en": "data-engineer-interview-questions",
        "title_tr": "Data Engineer",
        "title_en": "Data Engineer",
        "description_tr": "ETL, veri platformlari, veri ambarlari ve dagitik veri isleme",
        "description_en": "ETL, data platforms, data warehouses, and distributed data processing",
        "icon": "DatabaseZap",
        "sort_order": 20,
    },
    {
        "id": "cyber-security-specialist",
        "type": "POSITION",
        "slug_tr": "cyber-security-specialist-mulakat-sorulari",
        "slug_en": "cyber-security-specialist-interview-questions",
        "title_tr": "Cyber Security Specialist",
        "title_en": "Cyber Security Specialist",
        "description_tr": "Siber guvenlik, tehdit tespiti, olay mudahalesi ve guvenlik mimarisi",
        "description_en": "Cybersecurity, threat detection, incident response, and security architecture",
        "icon": "Shield",
        "sort_order": 21,
    },
    {
        "id": "business-analyst",
        "type": "POSITION",
        "slug_tr": "business-analyst-mulakat-sorulari",
        "slug_en": "business-analyst-interview-questions",
        "title_tr": "Business Analyst",
        "title_en": "Business Analyst",
        "description_tr": "Requirement analysis, process improvement, stakeholder management ve is analizi",
        "description_en": "Requirement analysis, process improvement, stakeholder management, and business analysis",
        "icon": "BriefcaseBusiness",
        "sort_order": 22,
    },
]

QUESTION_FILES = [
    "mobile_tr.json",
    "testing_tr.json",
    "linux_tr.json",
    "git_tr.json",
    "kotlin_tr.json",
    "swift_tr.json",
    "data-engineer_tr.json",
    "cyber-security-specialist_tr.json",
    "business-analyst_tr.json",
]


def sql_escape(value: str) -> str:
    return value.replace("${", "$ {").replace("'", "''")


def q(value: str) -> str:
    return f"'{sql_escape(value)}'"


def generate_category_sql() -> str:
    lines = [
        "-- V13: Seed additional categories for JSON-backed question sets",
        "-- Generated by scripts/generate_question_seed_migrations.py",
        "",
        "INSERT INTO categories (id, type, slug_tr, slug_en, title_tr, title_en, description_tr, description_en, icon, sort_order, active)",
        "VALUES",
    ]

    value_lines = []
    for category in CATEGORY_DEFS:
        value_lines.append(
            "  ({id}, {type}, {slug_tr}, {slug_en}, {title_tr}, {title_en}, {description_tr}, {description_en}, {icon}, {sort_order}, true)".format(
                id=q(category["id"]),
                type=q(category["type"]),
                slug_tr=q(category["slug_tr"]),
                slug_en=q(category["slug_en"]),
                title_tr=q(category["title_tr"]),
                title_en=q(category["title_en"]),
                description_tr=q(category["description_tr"]),
                description_en=q(category["description_en"]),
                icon=q(category["icon"]),
                sort_order=category["sort_order"],
            )
        )

    lines.append(",\n".join(value_lines))
    lines.append("ON CONFLICT (id) DO NOTHING;")
    lines.append("")
    return "\n".join(lines)


def generate_question_block(question: dict) -> str:
    options = question["options"]
    if len(options) != 4:
        raise ValueError(f"Question must have exactly 4 options: {question['question']}")

    lines = [
        "WITH q AS (",
        "  INSERT INTO questions (category_id, language, difficulty, question_text, explanation, correct_index, active)",
        "  VALUES ({category}, {language}, {difficulty}, {question_text}, {explanation}, {correct_index}, true)".format(
            category=q(question["category"]),
            language=q(question["language"]),
            difficulty=q(question["difficulty"]),
            question_text=q(question["question"]),
            explanation=q(question["explanation"]),
            correct_index=question["correctIndex"],
        ),
        "  RETURNING id",
        ")",
        "INSERT INTO question_options (question_id, option_index, option_text)",
        "  SELECT id, 0, {option_0} FROM q".format(option_0=q(options[0])),
        "UNION ALL",
        "  SELECT id, 1, {option_1} FROM q".format(option_1=q(options[1])),
        "UNION ALL",
        "  SELECT id, 2, {option_2} FROM q".format(option_2=q(options[2])),
        "UNION ALL",
        "  SELECT id, 3, {option_3} FROM q;".format(option_3=q(options[3])),
        "",
    ]
    return "\n".join(lines)


def generate_question_sql() -> str:
    lines = [
        "-- V14: Seed Turkish questions for additional categories",
        "-- Generated by scripts/generate_question_seed_migrations.py",
        "",
    ]

    for filename in QUESTION_FILES:
        path = DATA_DIR / filename
        with path.open(encoding="utf-8") as f:
            questions = json.load(f)

        if not questions:
            continue

        category = questions[0]["category"]
        lines.append(f"-- ── {category} ({len(questions)} soru) ──")
        for question in questions:
            lines.append(generate_question_block(question))

    return "\n".join(lines)


def main() -> None:
    CATEGORY_MIGRATION.write_text(generate_category_sql(), encoding="utf-8")
    QUESTION_MIGRATION.write_text(generate_question_sql(), encoding="utf-8")
    print(f"Wrote {CATEGORY_MIGRATION}")
    print(f"Wrote {QUESTION_MIGRATION}")


if __name__ == "__main__":
    main()
