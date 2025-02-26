create table event
(
    id                 bigint auto_increment
        primary key,
    app_id             int                                  not null comment '发起人id',
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

create index event_location_index
    on event (location);

create table friends
(
    id        int auto_increment
        primary key,
    user_id   int not null,
    friend_id int not null
)
    comment '好友列表';

create table friendsrequest
(
    id        int auto_increment
        primary key,
    app_id    int               not null comment '申请人id',
    friend_id int               not null comment '好友id',
    state     tinyint default 0 not null comment '状态，0是待验证，1是已同意'
)
    comment '好友申请';

create table items
(
    user_id int not null comment '用户id',
    item_id int not null comment '物品id'
)
    comment '用户的物品表';

create table user
(
    id              int auto_increment comment '用户id'
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
    user_id     int                                not null,
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

