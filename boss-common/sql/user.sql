drop database if exists `users_db`;
create database `users_db`;
use  `users_db`;

create table `user` (
    uid bigint primary key comment '唯一 id',
    name varchar(30) not null comment '用户名',
    password varchar(32) not null comment '密码',
    phone char(11) not null comment '手机号',
    avatar varchar(100) comment '头像链接',
    role tinyint(1) not null default 0 comment '角色，0 为求职者，1 为 HR',
    create_time datetime default current_timestamp comment '注册时间',
    update_time datetime default current_timestamp on update current_timestamp comment '更新信息时间',
    deleted tinyint(1) comment '软删除标记',

    key idx_phone(phone),
    unique key uk_phone_role(phone, role)
) comment '用户主表';

create table `user_job_apply` (
    id bigint primary key comment '全局求职状态显示',
    candidate_uid bigint not null comment '求职者 uid',
    hr_uid bigint not null comment 'HR uid',
    job_uid bigint not null comment '职位 uid',
    status tinyint(2) default 0 not null comment '状态',
    apply_msg varchar(500) comment '投递附言',
    create_time datetime default current_timestamp comment '投递时间',
    update_time datetime default current_timestamp on update current_timestamp comment '更新信息时间',
    deleted tinyint(1) comment '软删除标记',

    unique key uk_can_job(candidate_uid, job_uid),

    key idx_candidate (candidate_uid),
    key idx_job (job_uid),
    key idx_hr (hr_uid)
) comment '求职投递状态表';