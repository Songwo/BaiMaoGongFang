-- challenge 模块 2b：答题与进度（answer_records / exam_records / user_challenge_progress）

create table answer_records (
    id          varchar(36) primary key,
    user_id     varchar(36),
    question_id varchar(36),
    exam_id     varchar(64),
    user_answer varchar(255),
    correct     boolean not null default false,
    score_delta integer not null default 0,
    answer_time timestamp,
    cost_time   timestamp
);
create index idx_answer_records_user_id on answer_records (user_id);

create table exam_records (
    id               varchar(36) primary key,
    user_id          varchar(36),
    exam_id          varchar(64),
    submit_time      timestamp,
    cost_time        integer,
    total_score      integer not null default 0,
    correct_count    integer not null default 0,
    question_results jsonb
);
create index idx_exam_records_user_id on exam_records (user_id);

create table user_challenge_progress (
    user_id              varchar(36) primary key,
    unlocked_challenges  jsonb,
    completed_challenges jsonb
);
