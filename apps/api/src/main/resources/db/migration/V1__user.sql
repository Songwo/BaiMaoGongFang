-- auth/user 模块：用户、角色、用户画像
-- 混合映射：角色/兴趣正规化为关联表；纯值映射与 ID 数组用 jsonb 兜底

create table users (
    id                              varchar(36)  primary key,
    create_time                     timestamp,
    update_time                     timestamp,
    username                        varchar(30)  not null unique,
    password                        varchar(255),
    avatar                          varchar(255),
    email                           varchar(255),
    email_verified                  boolean      default false,
    email_verification_token        varchar(255),
    email_verification_token_expiry timestamp,
    last_login_time                 timestamp,
    total_score                     integer      not null default 0,
    correct_count                   integer      not null default 0,
    total_attempts                  integer      not null default 0,
    completed_questions             jsonb,
    vuln_scores                     jsonb,
    honorary_title                  jsonb,
    activity_points                 integer      not null default 0,
    last_check_in_date              timestamp,
    consecutive_check_in_days       integer      not null default 0,
    owned_rooms                     jsonb,
    active_labs                     jsonb,
    completed_labs                  jsonb
);
create index idx_users_email on users (email);
create index idx_users_total_score on users (total_score desc);

-- 用户角色（关联表，鉴权需查询）
create table user_roles (
    user_id varchar(36) not null references users (id) on delete cascade,
    role    varchar(50) not null,
    primary key (user_id, role)
);

-- 用户画像
create table user_profiles (
    id                   varchar(36) primary key,
    user_id              varchar(36),
    skill_level          integer,
    learning_preferences jsonb,
    last_update_time     timestamp
);
create index idx_user_profiles_user_id on user_profiles (user_id);

-- 画像兴趣（关联表，支持按兴趣检索）
create table user_profile_interests (
    user_profile_id varchar(36)  not null references user_profiles (id) on delete cascade,
    interest        varchar(255) not null,
    primary key (user_profile_id, interest)
);
