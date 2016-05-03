# --- !Ups

create table category (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  constraint pk_category primary key (id))
;

create table question (
  question_id               bigint auto_increment not null,
  text                      varchar(255),
  duration                  double,
  active                    tinyint(1) default 0,
  ordering                  integer,
  SCHOOL                    bigint,
  constraint pk_question primary key (question_id))
;

create table school (
  school_id                 bigint auto_increment not null,
  name                      varchar(255),
  constraint pk_school primary key (school_id))
;

create table user (
  discriminator             varchar(31) not null,
  id                        bigint auto_increment not null,
  name                      varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  approved                  tinyint(1) default 0,
  school_school_id          bigint,
  profile                   varchar(255),
  constraint pk_user primary key (id))
;

create table video (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  title                     varchar(255),
  description               longtext,
  thumbnail_path            varchar(255),
  approved                  tinyint(1) default 0,
  constraint pk_video primary key (id))
;

create table video_clip (
  id                        bigint auto_increment not null,
  video_clip_path           varchar(255),
  audio_clip_path           varchar(255),
  duration                  double,
  video_id                  bigint,
  question_question_id      bigint,
  constraint pk_video_clip primary key (id))
;


create table category_video (
  category_id                    bigint not null,
  video_id                       bigint not null,
  constraint pk_category_video primary key (category_id, video_id))
;
alter table question add constraint fk_question_school_1 foreign key (SCHOOL) references school (school_id) on delete cascade on update cascade;
create index ix_question_school_1 on question (SCHOOL);
alter table user add constraint fk_user_school_2 foreign key (school_school_id) references school (school_id) on delete cascade on update cascade;
create index ix_user_school_2 on user (school_school_id);
alter table video add constraint fk_video_user_3 foreign key (user_id) references user (id) on delete cascade on update cascade;
create index ix_video_user_3 on video (user_id);
alter table video_clip add constraint fk_video_clip_video_4 foreign key (video_id) references video (id) on delete cascade on update cascade;
create index ix_video_clip_video_4 on video_clip (video_id);
alter table video_clip add constraint fk_video_clip_question_5 foreign key (question_question_id) references question (question_id) on delete cascade on update cascade;
create index ix_video_clip_question_5 on video_clip (question_question_id);

alter table category_video add constraint fk_category_video_category_01 foreign key (category_id) references category (id) on delete cascade on update cascade;

alter table category_video add constraint fk_category_video_video_02 foreign key (video_id) references video (id) on delete cascade on update cascade;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table category;

drop table category_video;

drop table question;

drop table school;

drop table user;

drop table video;

drop table video_clip;

SET FOREIGN_KEY_CHECKS=1;

