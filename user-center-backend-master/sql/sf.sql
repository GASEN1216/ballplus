create table complaint
(
    id            bigint auto_increment comment '投诉ID'
        primary key,
    event_id      bigint                             not null comment '活动ID',
    complainer_id bigint                             not null comment '投诉人ID',
    complained_id bigint                             not null comment '被投诉人ID',
    content       varchar(500)                       not null comment '投诉内容',
    status        tinyint  default 0                 not null comment '状态 0-待处理 1-有效 2-无效',
    reject_reason varchar(255)                       null comment '拒绝原因',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '投诉记录表';

create index idx_complained_id
    on complaint (complained_id);

create index idx_complainer_id
    on complaint (complainer_id);

create index idx_event_id
    on complaint (event_id);

create table credit_record
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    user_id       bigint       not null comment '用户ID',
    credit_change int          not null comment '信誉分变动值（正数为增加，负数为减少）',
    change_type   tinyint      not null comment '变动原因类型：1-投诉扣分，2-取消活动扣分，3-退出活动扣分，4-管理员调整，5-其他原因',
    change_reason varchar(255) null comment '详细原因',
    relation_id   bigint       null comment '关联记录ID（如投诉ID、活动ID等）',
    create_time   datetime     not null comment '创建时间'
)
    comment '用户信誉分记录表';

create index idx_create_time
    on credit_record (create_time)
    comment '创建时间索引';

create index idx_user_id
    on credit_record (user_id)
    comment '用户ID索引';

create table event
(
    id                 bigint auto_increment
        primary key,
    app_id             bigint                              not null comment '发起人id',
    avatar             varchar(1024)                        not null,
    name               varchar(256)                         not null comment '活动名称',
    event_date         date                                 not null comment '开始时间（日期）',
    event_time         time                                 not null comment '开始时间 (当天时间)',
    event_timee        time                                 not null comment '结束时间 (当天时间)',
    location           varchar(256)                         not null comment '地点',
    location_detail    varchar(256)                         not null comment '详细地点',
    latitude           decimal(9, 7)                        not null comment '纬度',
    longitude          decimal(10, 7)                       not null comment '经度',
    total_participants int                                  not null comment '活动总人数',
    participants       int        default 1                 not null comment '活动人数',
    phone_number       varchar(25)                          not null comment '联系方式',
    type               tinyint    default 0                 not null comment '类型（0娱乐，1训练，2对打，3比赛）',
    remarks            varchar(1024)                        null comment '备注',
    labels             varchar(256)                         null comment '标签',
    limits             tinyint    default 0                 not null comment '限制(0无，1男，2女)',
    visibility         tinyint(1) default 1                 not null comment '可见性状态',
    level              tinyint    default 0                 not null comment '水平（小白 初学者 业余 中级 高级 专业）',
    fee_mode           tinyint    default 0                 not null comment '费用模式',
    fee                float      default 0                 not null comment '活动费用',
    penalty            tinyint(1) default 1                 not null comment '爽约惩罚',
    is_template        tinyint(1) default 0                 not null comment '是否模板',
    state              tinyint    default 0                 not null comment '状态 （0报名中，1已结束，2已取消）',
    is_delete          tinyint    default 0                 not null comment '逻辑删除',
    create_time        datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '上一次更新时间'
)
    comment '活动表';

create index event_event_date_index
    on event (event_date);

create index event_event_time_index
    on event (event_time);

create index event_location_index
    on event (location);

create index event_state_index
    on event (state);

create table event_cancel_record
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    event_id      bigint                             not null comment '活动ID',
    cancel_reason varchar(255)                       not null comment '取消原因',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '活动取消记录表';

create index idx_event_id
    on event_cancel_record (event_id)
    comment '活动ID索引';

create table friends
(
    id        int auto_increment
        primary key,
    user_id   bigint not null,
    friend_id bigint not null
)
    comment '好友列表';

create table friendsrequest
(
    id        int auto_increment
        primary key,
    app_id    bigint            not null comment '申请人id',
    friend_id bigint            not null comment '好友id',
    state     tinyint default 0 not null comment '状态，0是待验证，1是已同意'
)
    comment '好友申请';

create table items
(
    user_id bigint not null comment '用户id',
    item_id int    not null comment '物品id'
)
    comment '用户的物品表';

create table post
(
    id                  bigint auto_increment
        primary key,
    app_id              bigint                               not null,
    app_name            varchar(255)                         not null,
    avatar              varchar(1024)                        not null,
    grade               smallint   default 1                 not null,
    title               varchar(255)                         not null,
    content             text                                 not null,
    likes               int        default 0                 not null,
    comments            int        default 0                 not null,
    picture             varchar(255)                         null,
    is_delete           tinyint(1) default 0                 not null,
    create_time         datetime   default CURRENT_TIMESTAMP not null,
    update_time         datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    update_content_time datetime   default CURRENT_TIMESTAMP not null
);

create table comment
(
    id          bigint auto_increment
        primary key,
    app_id      bigint                               not null,
    app_name    varchar(255)                         not null,
    avatar      varchar(1024)                        not null,
    grade       smallint   default 1                 not null,
    post_id     bigint                               not null,
    content     text                                 not null,
    likes       int        default 0                 not null,
    comments    int        default 0                 not null comment '评论数',
    is_delete   tinyint(1) default 0                 not null,
    create_time datetime   default CURRENT_TIMESTAMP not null,
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_comment_post
        foreign key (post_id) references post (id)
            on update cascade on delete cascade
);

create index idx_post_id
    on comment (post_id);

create table product
(
    id          int auto_increment comment '商品ID'
        primary key,
    name        varchar(255)                       not null comment '商品名称',
    price       int                                not null comment '商品价格',
    image       varchar(1024)                      not null comment '商品图片URL',
    type        varchar(50)                        not null comment '商品类型',
    description varchar(500)                       null comment '商品描述',
    status      tinyint  default 1                 null comment '商品状态 0-下架 1-上架',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '商城商品表';

create table resource
(
    id          bigint auto_increment comment '主键'
        primary key,
    title       varchar(100)                       not null comment '标题',
    description text                               null comment '描述',
    cover_image varchar(255)                       null comment '封面图片URL',
    type        varchar(20)                        not null comment '资源类型：video/article',
    content     text                               not null comment '资源内容',
    views       int      default 0                 not null comment '浏览量',
    likes       int      default 0                 not null comment '点赞数',
    category    varchar(50)                        not null comment '分类',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '资源表';

create index idx_category
    on resource (category);

create index idx_create_time
    on resource (create_time);

create table score_history
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                             not null comment '用户ID',
    change_amount int                                not null comment '变动数量',
    type          varchar(50)                        not null comment '变动类型',
    description   varchar(255)                       null comment '变动描述',
    created_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '赛点变动历史';

create index idx_created_time
    on score_history (created_time);

create index idx_user_id
    on score_history (user_id);

create table sub_comment
(
    id          bigint auto_increment
        primary key,
    app_id      bigint                               not null,
    app_name    varchar(255)                         not null,
    avatar      varchar(1024)                        not null,
    grade       smallint   default 1                 not null,
    comment_id  bigint                               not null,
    content     text                                 not null,
    likes       int        default 0                 not null,
    is_delete   tinyint(1) default 0                 not null,
    create_time datetime   default CURRENT_TIMESTAMP not null,
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_sub_comment_comment
        foreign key (comment_id) references comment (id)
            on update cascade on delete cascade
);

create index idx_comment_id
    on sub_comment (comment_id);

create table user
(
    id              bigint auto_increment comment '用户id'
        primary key,
    user_name       varchar(256)                                       null comment '用户名',
    user_account    varchar(256)                                       not null comment '账户名',
    password        varchar(512)                                       not null comment '密码',
    avatar_url      varchar(1024)                                      null comment '头像url',
    gender          tinyint     default 0                              null comment '性别(0保密，1女生，2男生)',
    email           varchar(512)                                       null comment '电子邮箱',
    phone           varchar(128)                                       null comment '手机号码',
    grade           smallint    default 1                              not null comment '等级',
    exp             int         default 0                              not null comment '经验',
    sign_in         datetime                                           null comment '上一次签到时间',
    state           tinyint     default 0                              not null comment '用户状态',
    unblocking_time datetime                                           null comment '解封时间',
    is_delete       tinyint     default 0                              not null comment '逻辑删除',
    create_time     datetime    default CURRENT_TIMESTAMP              not null comment '创建账号时间',
    update_time     datetime    default CURRENT_TIMESTAMP              not null on update CURRENT_TIMESTAMP comment '更新时间',
    open_id         varchar(256)                                       null comment 'openid',
    birthday        date                                               null comment '生日',
    credit          int         default 100                            not null comment '信誉分',
    score           int         default 0                              not null comment '积分 ',
    description     varchar(20) default '这个人很冷酷，什么都没留下...' null comment '个性签名',
    label           varchar(255)                                       null comment '标签'
)
    comment '用户信息';

create index user_user_name_index
    on user (user_name);

create table user_event
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             not null,
    event_id    bigint                             not null,
    is_delete   int      default 0                 not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint user_event_event_id_fk
        foreign key (event_id) references event (id)
            on update cascade on delete cascade,
    constraint user_event_user_id_fk
        foreign key (user_id) references user (id)
            on update cascade on delete cascade
);

create table user_favorite
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    resource_id bigint                             not null comment '资源ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '收藏时间',
    constraint uk_user_resource
        unique (user_id, resource_id)
)
    comment '用户收藏表';

ALTER TABLE user ADD CONSTRAINT uk_open_id UNIQUE (open_id);

-- 为 friends 表添加 (user_id, friend_id) 的唯一约束，防止重复好友关系
ALTER TABLE friends ADD CONSTRAINT uk_user_friend UNIQUE (user_id, friend_id);

-- 为 friendsrequest 表添加 (app_id, friend_id) 的唯一约束，防止重复申请
ALTER TABLE friendsrequest ADD CONSTRAINT uk_app_friend UNIQUE (app_id, friend_id);

-- 为 items 表添加 (user_id, item_id) 的联合主键，确保用户物品唯一性
-- 注意：这假设一个用户同一种 item_id 只有一个记录。如果允许堆叠数量，需要不同设计。
ALTER TABLE items ADD CONSTRAINT pk_user_item PRIMARY KEY (user_id, item_id);

-- 为 user_event 表添加 (user_id, event_id) 的唯一约束，防止重复报名
ALTER TABLE user_event ADD CONSTRAINT uk_user_event UNIQUE (user_id, event_id);


-- -------- 添加/优化索引 (基于推断的业务查询场景) --------

-- user 表：
-- (已存在 user_name 索引)
-- 添加创建时间索引，便于按注册时间查询或排序
CREATE INDEX idx_user_create_time ON user (create_time);
-- 添加用户状态索引，便于后台按状态筛选用户
CREATE INDEX idx_user_state ON user (state);

-- event 表：
-- (已存在 event_date, event_time, location, state 索引)
-- 添加发起人ID索引，便于查询“我发起的活动”
CREATE INDEX idx_event_app_id ON event (app_id);
-- 添加类型索引，便于按活动类型筛选
CREATE INDEX idx_event_type ON event (type);
-- 添加复合索引 (状态, 日期)，提高按状态和日期查询活动的效率 (如查询进行中的活动)
CREATE INDEX idx_event_state_date ON event (state, event_date);
-- 添加是否为模板的索引，如果模板查询常用
CREATE INDEX idx_event_is_template ON event (is_template);
-- **地理空间索引 (如果需要按距离搜索附近活动)**
-- 步骤1: 添加 POINT 类型的列 (如果尚不存在)
-- ALTER TABLE event ADD COLUMN location_point POINT NULL COMMENT '经纬度点';
-- 步骤2: 填充数据 (只需执行一次)
-- UPDATE event SET location_point = POINT(longitude, latitude) WHERE longitude IS NOT NULL AND latitude IS NOT NULL;
-- 步骤3: 创建空间索引
-- CREATE SPATIAL INDEX idx_location_spatial ON event(location_point);

-- post 表：
-- 添加 (作者ID, 创建时间) 复合索引，高效查询用户帖子并按时间排序
CREATE INDEX idx_post_app_id_time ON post (app_id, create_time);
-- 添加创建时间索引，用于全局帖子流按时间排序
CREATE INDEX idx_post_create_time ON post (create_time);
-- **全文索引 (如果需要搜索标题和内容)**
CREATE FULLTEXT INDEX idx_ft_post_title_content ON post (title, content);

-- comment 表：
-- (已存在 post_id 索引)
-- 添加 (帖子ID, 创建时间) 复合索引，高效查询帖子评论并按时间排序
CREATE INDEX idx_comment_post_id_time ON comment (post_id, create_time);
-- 添加评论者ID索引，便于查询用户发表的评论
CREATE INDEX idx_comment_app_id ON comment (app_id);

-- sub_comment 表：
-- (已存在 comment_id 索引)
-- 添加 (父评论ID, 创建时间) 复合索引，高效查询评论回复并按时间排序
CREATE INDEX idx_sub_comment_id_time ON sub_comment (comment_id, create_time);
-- 添加回复者ID索引
CREATE INDEX idx_sub_comment_app_id ON sub_comment (app_id);

-- friendsrequest 表：
-- 添加 (好友ID, 状态) 复合索引，高效查询发给某用户的待处理请求
CREATE INDEX idx_friendreq_friend_id_state ON friendsrequest (friend_id, state);

-- user_event 表：
-- 添加 event_id 索引，便于查询某活动的所有参与者
CREATE INDEX idx_userevent_event_id ON user_event (event_id);

-- complaint 表：
-- 添加 (状态, 创建时间) 复合索引，便于后台处理待处理投诉
CREATE INDEX idx_complaint_status_time ON complaint (status, create_time);

-- credit_record 表：
-- (已有 user_id 和 create_time 索引)
-- 可考虑添加更具体的复合索引，例如按用户和类型查询历史记录
CREATE INDEX idx_credit_user_type_time ON credit_record (user_id, change_type, create_time);

-- resource 表：
-- (已有 category 和 create_time 索引)
-- 添加 (分类, 创建时间) 复合索引，优化按分类筛选并排序
CREATE INDEX idx_resource_category_time ON resource (category, create_time);
-- 如果按类型和分类筛选很常见，可以考虑三字段索引
CREATE INDEX idx_resource_type_category_time ON resource (type, category, create_time);

-- product 表：
-- 添加 (状态, 类型) 复合索引，便于按状态和类型筛选商品
CREATE INDEX idx_product_status_type ON product (status, type);

-- score_history 表：
-- (已有 user_id 和 created_time 索引)
-- 如果按类型查询用户积分历史很常见
CREATE INDEX idx_score_user_type_time ON score_history (user_id, type, created_time);


-- -------- 移除冗余索引 --------

-- user_favorite 表中 uk_user_resource (user_id, resource_id) 唯一索引已存在
-- 它能满足按 user_id 查询的需求，所以 idx_user_id 是冗余的
-- 如果 idx_user_id 确实存在，则可以移除
DROP INDEX idx_user_id ON user_favorite;