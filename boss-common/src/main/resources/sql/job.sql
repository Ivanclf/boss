drop database if exists `job_db`;
create database `job_db`;
use  `job_db`;

create table `job` (
    uid bigint auto_increment primary key comment '全局唯一id',
    hr_uid bigint not null comment '发布者 uid',
    title varchar(64) not null comment '职位名称',
    description text comment '职位描述',
    requirement text comment '要求',
    city varchar(32) comment '工作城市',
    salary_min int comment '最低月薪',
    salary_max int comment '最高月薪',
    status tinyint(1) default 0 comment '是否开放',
    publish_time datetime default current_timestamp comment '发布时间',
    update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
    deleted tinyint(1) default 0 not null comment '软删除标签',

    key idx_hr(hr_uid),
    key idx_city_salary(city, salary_min, salary_max),
    fulltext key ft_title_desc (title, description)
) comment '职位表';

create table `job_tag` (
    id bigint auto_increment primary key comment '全局唯一id',
    job_uid bigint not null comment '工作 id',
    tag varchar(32) not null comment '技术栈标签',
    unique key uk_job_tag(job_uid, tag),
    key idx_tag(tag)
) comment '职位技术标签';