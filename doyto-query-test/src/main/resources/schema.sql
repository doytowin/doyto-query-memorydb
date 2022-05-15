SET DATABASE SQL SYNTAX MYS TRUE;

drop table t_user if exists;
create table t_user
(
  id bigint generated by default as identity (start with 1),
  email varchar(255),
  mobile varchar(255),
  nickname varchar(255),
  password varchar(255),
  user_level varchar(255),
  username varchar(255),
  memo varchar(255),
  valid boolean DEFAULT TRUE,
  create_time timestamp,
  create_user_id bigint,
  update_time timestamp,
  update_user_id bigint,
  primary key (id)
);

drop table user_detail if exists;
create table user_detail (
  id bigint,
  address varchar(255),
  primary key (id)
);

drop table menu_01 if exists;
create table menu_01 (
  id integer generated by default as identity (start with 1),
  create_time timestamp,
  create_user_id bigint,
  update_time timestamp,
  update_user_id bigint,
  platform varchar(15),
  memo varchar(255),
  menu_name varchar(255),
  parent_id integer,
  valid boolean,
  primary key (id)
);

drop table menu if exists;
create table menu (
  id integer generated by default as identity (start with 1),
  create_time timestamp,
  create_user_id bigint,
  update_time timestamp,
  update_user_id bigint,
  platform varchar(15),
  memo varchar(255),
  menu_name varchar(255),
  parent_id integer,
  valid boolean,
  primary key (id)
);

drop table t_role if exists;
create table t_role(
  id bigint generated by default as identity (start with 1),
  role_name VARCHAR(100) not null,
  role_code VARCHAR(100) not null,
  valid boolean DEFAULT TRUE,
  create_time timestamp not null default CURRENT_TIMESTAMP,
  create_user_id bigint,
  update_time timestamp,
  update_user_id bigint,
  primary key (id)
);

drop table j_user_and_role if exists;
create table j_user_and_role (user_id bigint, role_id int, create_user_id bigint);
alter table j_user_and_role add constraint j_user_and_role_unique_constraint unique (user_id, role_id);

drop table t_perm if exists;
CREATE TABLE t_perm
(
  id        bigint generated BY DEFAULT AS IDENTITY (start WITH 1),
  perm_name  VARCHAR(100) not null,
  valid     boolean DEFAULT TRUE,
  primary key (id)
);

drop table j_role_and_perm if exists;
CREATE TABLE j_role_and_perm (role_id int, perm_id int);
alter table j_role_and_perm add constraint j_role_and_perm_unique_constraint unique (role_id, perm_id);
