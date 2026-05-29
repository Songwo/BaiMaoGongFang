-- content 模块：资讯 / 公告 / 更新日志 / 视频 / 视频分类 / 学习内容

create table news (
    id             varchar(36) primary key,
    title          varchar(512),
    type           varchar(64),
    summary        text,
    full_content   text,
    impact         text,
    solution       text,
    reference_list jsonb,           -- references 是 PG 保留字，列名改 reference_list
    create_time    timestamp,
    update_time    timestamp
);

create table announcement (
    id          varchar(36) primary key,
    title       varchar(512),
    content     text,
    type        varchar(64),
    tag         varchar(64),
    create_time timestamp,
    creator_id  varchar(64),
    is_active   boolean
);

create table changelog (
    id          varchar(36) primary key,
    version     varchar(64),
    type        varchar(64),
    content     text,
    create_time timestamp,
    creator_id  varchar(64),
    is_active   boolean
);

create table video_item (
    id          varchar(36) primary key,
    title       varchar(512),
    url         varchar(1024),
    image_url   varchar(1024),
    description text,
    categories  varchar(255),
    create_time timestamp
);
create index idx_video_item_create_time on video_item (create_time desc);

create table video_category (
    id          varchar(36) primary key,
    challengel1 varchar(255),
    challengel2 varchar(255),
    challengel3 varchar(255),
    challengel4 varchar(255),
    challengel5 varchar(255),
    challengel6 varchar(255)
);

create table learning_content (
    id               varchar(36) primary key,
    title            varchar(512),
    description      text,
    content_type     varchar(64),
    difficulty_level integer,
    categories       jsonb,
    tags             jsonb,
    metadata         jsonb,
    create_time      timestamp,
    update_time      timestamp,
    learning_stage   varchar(64)
);
