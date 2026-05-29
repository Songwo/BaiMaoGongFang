-- challenge 模块 2a：题库目录（challenges / questions / category / hole / lab_flag）

create table challenges (
    id               varchar(36) primary key,
    title            varchar(255),
    description      text,
    difficulty       integer not null default 0,
    task             text,
    flag             varchar(255),
    score            integer not null default 0,
    images           jsonb,
    duration_minutes integer not null default 0
);

create table questions (
    id               varchar(36) primary key,
    title            text,
    category         varchar(255),
    options          jsonb,
    answer           text,
    explanation      text,
    created_at       varchar(64),
    updated_at       varchar(64),
    score            integer not null default 10,
    difficulty_level integer not null default 0
);
create index idx_questions_category on questions (category);

create table category (
    id          varchar(36) primary key,
    challengel1 varchar(255),
    challengel2 varchar(255),
    challengel3 varchar(255),
    challengel4 varchar(255),
    challengel5 varchar(255),
    challengel6 varchar(255)
);

create table hole (
    id                varchar(36) primary key,
    cve_id            varchar(64),
    title             varchar(512),
    description       text,
    severity          varchar(32),
    cvss_score        varchar(32),
    published_date    timestamp,
    affected_versions jsonb,
    solutions         text,
    created_at        timestamp,
    updated_at        timestamp,
    type              varchar(64)
);

create table lab_flag (
    id         varchar(36) primary key,
    image_name varchar(255),
    flag       varchar(255)
);
