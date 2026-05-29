-- community 模块：帖子 / 评论 / 反馈

create table posts (
    id             varchar(36) primary key,
    author_id      varchar(64),
    username       varchar(64),
    honorary_title varchar(255),
    title          varchar(255),
    section        varchar(128),
    content        text,
    cover_image    varchar(512),
    reply_count    integer not null default 0,
    avatar         varchar(512),
    timestamp      timestamp
);
create index idx_posts_section on posts (section);
create index idx_posts_username on posts (username);

create table comments (
    id             varchar(36) primary key,
    post_id        varchar(36),
    author_id      varchar(64),
    honorary_title varchar(255),
    username       varchar(64),
    content        text,
    timestamp      timestamp,
    avatar         varchar(512)
);
create index idx_comments_post_id on comments (post_id);
create index idx_comments_username on comments (username);

create table feedback (
    id          varchar(36) primary key,
    type        varchar(32),
    title       varchar(255),
    content     text,
    contact     varchar(255),
    image_urls  jsonb,
    status      varchar(32),
    create_time timestamp,
    update_time timestamp
);
create index idx_feedback_status on feedback (status);
