create table article_generator.article
(
    id           bigint auto_increment comment '主键'
        primary key,
    category_id  bigint                                not null comment '栏目ID',
    title        varchar(200)                          not null comment '文章标题',
    content      longtext                              null comment '文章内容',
    summary      varchar(500)                          null comment '摘要',
    cover_image  varchar(255)                          null comment '封面图URL',
    publish_date date                                  null comment '发布日期',
    view_count   int         default 0                 null comment '阅读量',
    status       tinyint     default 1                 null comment '状态(0草稿/1已发布)',
    create_time  datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    content_type varchar(20) default 'markdown'        null
)
    comment '文章表';

create index idx_category_id
    on article_generator.article (category_id);

create index idx_publish_date
    on article_generator.article (publish_date);

create index idx_status
    on article_generator.article (status);

create table article_generator.category
(
    id              bigint auto_increment comment '主键'
        primary key,
    name            varchar(50)                        not null comment '栏目名称',
    code            varchar(50)                        not null comment '栏目编码',
    prompt_template text                               null comment 'AI生成提示词模板',
    sort_order      int      default 0                 null comment '排序',
    status          tinyint  default 1                 null comment '状态(0禁用/1启用)',
    create_time     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_code
        unique (code)
)
    comment '栏目表';

create table article_generator.user
(
    id          bigint auto_increment comment '主键'
        primary key,
    username    varchar(50)                           not null comment '用户名',
    password    varchar(255)                          not null comment '密码(BCrypt加密)',
    nickname    varchar(50)                           null comment '昵称',
    email       varchar(100)                          null comment '邮箱',
    avatar      varchar(255)                          null comment '头像URL',
    role        varchar(20) default 'USER'            not null comment '角色(ADMIN/USER)',
    status      tinyint     default 1                 null comment '状态(0禁用/1启用)',
    create_time datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_email
        unique (email),
    constraint uk_username
        unique (username)
)
    comment '用户表';

