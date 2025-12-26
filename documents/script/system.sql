drop
database if exists hubbo;
create
database hubbo with encoding 'UTF-8';

-- 用户信息表
DROP TABLE IF EXISTS t_user;
CREATE TABLE IF NOT EXISTS t_user
(
    user_id            bigint       NOT NULL,
    user_name          varchar(100) NOT NULL,
    phone              char(11)     NOT NULL UNIQUE,
    passwd             varchar(100),
    profile_url        varchar(255),
    enabled            boolean               DEFAULT true,
    deleted            boolean               DEFAULT false NOT NULL,
    create_by          bigint       NOT NULL,
    create_time        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by          bigint,
    update_time        timestamp             DEFAULT CURRENT_TIMESTAMP,
    recent_online_time timestamp             DEFAULT CURRENT_TIMESTAMP,
    description        varchar(255),
    tenant_id          bigint       NOT NULL,
    CONSTRAINT t_user_pk PRIMARY KEY (user_id)
    );

COMMENT ON TABLE t_user IS '用户基础信息表';
COMMENT ON COLUMN t_user.user_id IS '用户编号，分布式id';
COMMENT ON COLUMN t_user.user_name IS '用户名';
COMMENT ON COLUMN t_user.phone IS '手机号';
COMMENT ON COLUMN t_user.passwd IS '用户密码，不要存储明文';
COMMENT ON COLUMN t_user.profile_url IS '头像url地址';
COMMENT ON COLUMN t_user.enabled IS '是否启用';
COMMENT ON COLUMN t_user.deleted IS '逻辑删除字段';
COMMENT ON COLUMN t_user.create_by IS '创建人，用户自行注册的话值就是自己的id';
COMMENT ON COLUMN t_user.create_time IS '创建时间';
COMMENT ON COLUMN t_user.update_by IS '更新人';
COMMENT ON COLUMN t_user.update_time IS '更新时间';
COMMENT ON COLUMN t_user.recent_online_time IS '最近一次的上线时间';
COMMENT ON COLUMN t_user.description IS '对用户的备注信息';
comment on column t_user.tenant_id is '租户id';


-- 角色信息表
DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role
(
    role_id     bigint                  NOT NULL,
    role_name   varchar(255)            NOT NULL,
    enabled     boolean   DEFAULT true  NOT NULL,
    deleted     boolean   DEFAULT false NOT NULL,
    create_by   bigint                  NOT NULL,
    create_time timestamp               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   bigint,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    tenant_id   bigint                  not null,
    CONSTRAINT t_role_pk PRIMARY KEY (role_id)
);

COMMENT ON TABLE t_role IS '角色基础信息表';
COMMENT ON COLUMN t_role.role_id IS '角色id';
COMMENT ON COLUMN t_role.role_name IS '角色名称';
COMMENT ON COLUMN t_role.enabled IS '是否启用';
COMMENT ON COLUMN t_role.deleted IS '逻辑删除字段';
COMMENT ON COLUMN t_role.create_by IS '创建人';
COMMENT ON COLUMN t_role.create_time IS '创建时间';
COMMENT ON COLUMN t_role.update_by IS '更新人';
COMMENT ON COLUMN t_role.update_time IS '更新时间';
comment on column t_role.tenant_id is '租户id';


-- 用户角色关联表
DROP TABLE IF EXISTS t_user_role;
CREATE TABLE t_user_role
(
    id          bigint                  NOT NULL,
    user_id     bigint                  NOT NULL,
    role_id     bigint                  NOT NULL,
    enabled     boolean   DEFAULT true  NOT NULL,
    deleted     boolean   DEFAULT false NOT NULL,
    create_by   bigint                  NOT NULL,
    create_time timestamp               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   bigint,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    description varchar(255),
    tenant_id   bigint                  not null,
    CONSTRAINT t_user_role_pk PRIMARY KEY (id)
);

COMMENT ON TABLE t_user_role IS '用户角色关联信息表';
COMMENT ON COLUMN t_user_role.id IS '主键';
COMMENT ON COLUMN t_user_role.user_id IS '用户编号，t_user主键';
COMMENT ON COLUMN t_user_role.role_id IS '角色编号，t_role主键';
COMMENT ON COLUMN t_user_role.enabled IS '是否启用';
COMMENT ON COLUMN t_user_role.deleted IS '逻辑删除';
COMMENT ON COLUMN t_user_role.create_by IS '创建人';
COMMENT ON COLUMN t_user_role.create_time IS '创建时间';
COMMENT ON COLUMN t_user_role.update_by IS '更新人';
COMMENT ON COLUMN t_user_role.update_time IS '更新时间';
COMMENT ON COLUMN t_user_role.description IS '备注描述信息';
comment on column t_user_role.tenant_id is '租户id';


-- 权限信息
DROP TABLE IF EXISTS t_permission;
CREATE TABLE t_permission
(
    permission_id   bigint                  NOT NULL,
    permission_code varchar(255)            NOT NULL,
    permission_name varchar(255)            NOT NULL,
    enabled         boolean   DEFAULT true  NOT NULL,
    deleted         boolean   DEFAULT false NOT NULL,
    create_by       bigint                  NOT NULL,
    create_time     timestamp               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       bigint,
    update_time     timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT t_permission_pk PRIMARY KEY (permission_id),
    tenant_id       bigint                  not null,
    UNIQUE (permission_code)
);

COMMENT ON TABLE t_permission IS '权限信息表';
COMMENT ON COLUMN t_permission.permission_id IS '权限id';
COMMENT ON COLUMN t_permission.permission_code IS '权限字符串，三段式，module.menu.action,module模块，如system，auth，menu可以是一级菜单也可以是二级三级菜单，action即要执行的动作';
COMMENT ON COLUMN t_permission.permission_name IS '权限名称';
COMMENT ON COLUMN t_permission.enabled IS '是否启用';
COMMENT ON COLUMN t_permission.deleted IS '逻辑删除字段';
COMMENT ON COLUMN t_permission.create_by IS '创建人';
COMMENT ON COLUMN t_permission.create_time IS '创建时间';
COMMENT ON COLUMN t_permission.update_by IS '更新人';
COMMENT ON COLUMN t_permission.update_time IS '更新时间';
COMMENT ON COLUMN t_permission.tenant_id IS '租户id';


-- 权限角色关联表
DROP TABLE IF EXISTS t_role_permission;
CREATE TABLE t_role_permission
(
    id            bigint    NOT NULL,
    role_id       bigint    NOT NULL,
    permission_id bigint    NOT NULL,
    enabled       boolean            DEFAULT true,
    create_by     bigint    NOT NULL,
    create_time   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     bigint,
    update_time   timestamp          DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT t_role_permission_pk PRIMARY KEY (id),
    tenant_id     bigint    not null,
    CONSTRAINT t_role_permission_uk unique (role_id, permission_id, enabled)
);

COMMENT ON TABLE t_role_permission IS '角色权限关联信息表';
COMMENT ON COLUMN t_role_permission.id IS '主键编号';
COMMENT ON COLUMN t_role_permission.role_id IS '角色id';
COMMENT ON COLUMN t_role_permission.permission_id IS '权限编号';
COMMENT ON COLUMN t_role_permission.enabled IS '是否启用';
COMMENT ON COLUMN t_role_permission.create_by IS '创建人';
COMMENT ON COLUMN t_role_permission.create_time IS '创建时间';
COMMENT ON COLUMN t_role_permission.update_by IS '更新人';
COMMENT ON COLUMN t_role_permission.update_time IS '更新时间';
COMMENT ON COLUMN t_role_permission.tenant_id IS '租户id';


-- 菜单信息表
DROP TABLE IF EXISTS t_menu;
CREATE TABLE t_menu
(
    menu_id       bigint       NOT NULL,
    menu_name     varchar(255) NOT NULL,
    path          varchar(255) NOT NULL,
    component     varchar(255) NOT NULL,
    level         char(1)      NOT NULL,
    title         varchar(255),
    icon          varchar(255),
    auth          boolean               DEFAULT true,
    keep_alive    boolean               DEFAULT true,
    display_order int,
    menu_type     char(1)               DEFAULT 'M' CHECK ( menu_type IN ('C', 'M', 'L')),
    link          varchar(255),
    upper_menu_id bigint,
    enabled       boolean               DEFAULT true,
    deleted       boolean               DEFAULT false,
    create_by     bigint       NOT NULL,
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     bigint,
    update_time   timestamp             DEFAULT CURRENT_TIMESTAMP,
    tenant_id     bigint       not null,
    CONSTRAINT t_menu_pk PRIMARY KEY (menu_id)
);

COMMENT ON TABLE t_menu IS '菜单配置信息表，对应前端的路由信息';
COMMENT ON COLUMN t_menu.menu_id IS '菜单编号';
COMMENT ON COLUMN t_menu.menu_name IS '菜单名';
COMMENT ON COLUMN t_menu.path IS 'URI资源路径';
COMMENT ON COLUMN t_menu.component IS '渲染组件的路径';
COMMENT ON COLUMN t_menu.level IS '菜单层级';
COMMENT ON COLUMN t_menu.title IS '标题';
COMMENT ON COLUMN t_menu.icon IS '菜单icon';
COMMENT ON COLUMN t_menu.auth IS '是否需要认证';
COMMENT ON COLUMN t_menu.keep_alive IS '是否缓存组件';
COMMENT ON COLUMN t_menu.display_order IS '序号，查询结果根据此字段排序';
COMMENT ON COLUMN t_menu.menu_type IS ' 菜单类型，如目录C，菜单M，超链接L ';
COMMENT ON COLUMN t_menu.link IS '链接的地址，当menu_type为L时，此字段值有意义';
COMMENT ON COLUMN t_menu.upper_menu_id IS '父级菜单编号';
COMMENT ON COLUMN t_menu.enabled IS '是否启用';
COMMENT ON COLUMN t_menu.deleted IS '逻辑删除字段';
COMMENT ON COLUMN t_menu.create_by IS '创建人';
COMMENT ON COLUMN t_menu.create_time IS '创建时间';
COMMENT ON COLUMN t_menu.update_by IS '更新人';
COMMENT ON COLUMN t_menu.update_time IS '更新时间';
COMMENT ON COLUMN t_menu.tenant_id IS '租户id';


-- 菜单权限关联表
DROP TABLE IF EXISTS t_menu_permission;
CREATE TABLE t_menu_permission
(
    id            bigint    NOT NULL,
    permission_id bigint    NOT NULL,
    menu_id       bigint    NOT NULL,
    enabled       boolean            DEFAULT true,
    deleted       boolean            DEFAULT false,
    create_by     bigint    NOT NULL,
    create_time   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     bigint,
    update_time   timestamp          DEFAULT CURRENT_TIMESTAMP,
    tenant_id     bigint    not null,
    CONSTRAINT t_menu_permission_pk PRIMARY KEY (id)
);

COMMENT ON TABLE t_menu_permission IS '菜单权限关联表';
COMMENT ON COLUMN t_menu_permission.id IS '关系编号';
COMMENT ON COLUMN t_menu_permission.permission_id IS '权限编号';
COMMENT ON COLUMN t_menu_permission.menu_id IS '菜单编号';
COMMENT ON COLUMN t_menu_permission.enabled IS '是否启用';
COMMENT ON COLUMN t_menu_permission.deleted IS '是否删除';
COMMENT ON COLUMN t_menu_permission.create_by IS '创建人';
COMMENT ON COLUMN t_menu_permission.create_time IS '创建时间';
COMMENT ON COLUMN t_menu_permission.update_by IS '更新人';
COMMENT ON COLUMN t_menu_permission.update_time IS '更新时间';
COMMENT ON COLUMN t_menu_permission.tenant_id IS '租户id';


-- 按钮权限表
DROP TABLE IF EXISTS t_button_permission;
CREATE TABLE t_button_permission
(
    id                     bigint       NOT NULL,
    button_permission_code varchar(255) NOT NULL,
    button_name            varchar(255) NOT NULL,
    permission_id          int          NOT NULL,
    enabled                boolean               DEFAULT true,
    deleted                boolean               DEFAULT false,
    create_by              bigint       NOT NULL,
    create_time            timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by              bigint,
    update_time            timestamp             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT t_button_permission_pk PRIMARY KEY (id),
    tenant_id              bigint       not null,
    UNIQUE (button_permission_code)
);

COMMENT ON TABLE t_button_permission IS '按钮权限关联信息表';
COMMENT ON COLUMN t_button_permission.id IS '按钮权限编号';
COMMENT ON COLUMN t_button_permission.button_permission_code IS '按钮权限字符';
COMMENT ON COLUMN t_button_permission.button_name IS '按钮名称';
COMMENT ON COLUMN t_button_permission.enabled IS '是否启用';
COMMENT ON COLUMN t_button_permission.deleted IS '逻辑删除字段';
COMMENT ON COLUMN t_button_permission.create_by IS '创建人';
COMMENT ON COLUMN t_button_permission.create_time IS '创建时间';
COMMENT ON COLUMN t_button_permission.update_by IS '更新人';
COMMENT ON COLUMN t_button_permission.update_time IS '更新时间';
COMMENT ON COLUMN t_button_permission.permission_id IS '权限编号';
COMMENT ON COLUMN t_button_permission.tenant_id IS '租户';

drop table if exists t_dict_data;
create table t_dict_data
(

    dict_id     bigint      not null,
    dict_code   varchar(65) not null,
    code_value  varchar(255),
    enabled     boolean              DEFAULT true,
    deleted     boolean              DEFAULT false NOT NULL,
    create_by   bigint      NOT NULL,
    create_time timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   bigint,
    update_time timestamp            DEFAULT CURRENT_TIMESTAMP,
    description varchar(255),
    tenant_id   bigint      not null,
    constraint t_dict_data_pk primary key (dict_id)
);

comment on table t_dict_data is '字典表';
comment on column t_dict_data.dict_id is '字典编号';
comment on column t_dict_data.dict_code is '码值类型';
comment on column t_dict_data.code_value is '码值';
comment on column t_dict_data.enabled is '是否启用';
comment on column t_dict_data.deleted is '逻辑删除标志位';
comment on column t_dict_data.create_by is '创建人';
comment on column t_dict_data.create_time is '创建时间';
comment on column t_dict_data.update_by is '更新人';
comment on column t_dict_data.update_time is '更新时间';
comment on column t_dict_data.description is '备注信息';
comment on column t_dict_data.tenant_id is '租户id';