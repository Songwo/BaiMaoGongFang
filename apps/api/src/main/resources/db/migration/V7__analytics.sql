-- analytics / user 补充模块：称号 / 邀请码 / 奖励 / 统计 / 学习行为

create table honorary_title (
    id          varchar(36) primary key,
    title       varchar(255),
    description text,
    activite    integer not null default 100
);

create table invite_code (
    id         varchar(36) primary key,
    code       varchar(64),
    inviter    varchar(64),
    invitee    varchar(64),
    created_at timestamp,
    used       boolean not null default false,
    used_at    timestamp
);

create table reward (
    id      varchar(36) primary key,
    name    varchar(255),
    context text
);

create table stats (
    id             varchar(36) primary key,
    user_id        varchar(36),
    stat_date      varchar(32),
    post_count     integer not null default 0,
    comment_count  integer not null default 0,
    daily_visits   bigint  not null default 0,
    user_count     bigint  not null default 0,
    total_posts    bigint  not null default 0,
    total_comments bigint  not null default 0,
    created_date   date
);
create index idx_stats_date on stats (stat_date);
create index idx_stats_user_date on stats (user_id, stat_date);

create table user_learning_behavior (
    id               varchar(36) primary key,
    user_id          varchar(36),
    content_type     varchar(64),
    content_id       varchar(64),
    interaction_type varchar(64),
    interaction_time timestamp,
    duration         integer,
    score            double precision,
    metadata         jsonb
);
create index idx_ulb_user_id on user_learning_behavior (user_id);
