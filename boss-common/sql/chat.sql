drop database if exists `chat_db`;
create database `chat_db`;
use  `chat_db`;

create table `chat_record` (
    id bigint auto_increment primary key comment '全局唯一id',
    status tinyint(1) default 0 not null comment '状态，0 为和 hr 对话，1 为和 ai 对话',
    from_uid bigint not null comment '发送方 uid',
    to_uid bigint not null comment '接收方 uid',
    job_uid bigint comment '关联职位',
    create_time datetime default current_timestamp comment '消息发送时间',
    context text not null,
    deleted tinyint(1) comment '软删除标记',

    key idx_from (from_uid),
    key idx_to (to_uid),
    key idx_time (create_time)
) comment '聊天记录表';