-- chat 模块：聊天室 / 聊天消息
-- 注：原 Mongo 消息集合名混用 messages/chat_message，统一为单表 chat_message

create table chat_room (
    id           varchar(36) primary key,
    title        varchar(255),
    category     varchar(128),
    password     varchar(255),
    expire_at    timestamp,
    participants jsonb
);
create index idx_chat_room_category on chat_room (category);

create table chat_message (
    id        varchar(36) primary key,
    room_id   varchar(36),
    sender_id varchar(36),
    content   text,
    timestamp timestamp
);
create index idx_chat_message_room_id on chat_message (room_id);
