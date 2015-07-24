# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table assignment (
  id                        bigint not null,
  due_date                  varchar(255),
  school_class_id           bigint,
  kind_of_assignment        varchar(255),
  spanner                   varchar(255),
  description               varchar(255),
  finished                  boolean,
  month                     integer,
  day                       integer,
  year                      integer,
  total                     integer,
  foreign_id                varchar(40),
  constraint pk_assignment primary key (id))
;

create table note (
  id                        bigint not null,
  title                     varchar(255),
  notes                     varchar(255),
  school_class_id           bigint,
  foreign_id                varchar(40),
  constraint pk_note primary key (id))
;

create table parent (
  id                        varchar(40) not null,
  email                     varchar(255),
  password                  varchar(255),
  name                      varchar(255),
  salt                      varchar(255),
  constraint pk_parent primary key (id))
;

create table school_class (
  id                        bigint not null,
  subject                   varchar(255),
  color                     varchar(255),
  teacher_id                varchar(40),
  password                  varchar(255),
  constraint pk_school_class primary key (id))
;

create table student (
  id                        varchar(40) not null,
  email                     varchar(255),
  password                  varchar(255),
  name                      varchar(255),
  salt                      varchar(255),
  grade                     varchar(255),
  parent_id                 varchar(40),
  constraint pk_student primary key (id))
;

create table teacher (
  id                        varchar(40) not null,
  email                     varchar(255),
  password                  varchar(255),
  name                      varchar(255),
  salt                      varchar(255),
  constraint pk_teacher primary key (id))
;


create table school_class_student (
  school_class_id                bigint not null,
  student_id                     varchar(40) not null,
  constraint pk_school_class_student primary key (school_class_id, student_id))
;
create sequence assignment_seq;

create sequence note_seq;

create sequence school_class_seq;

alter table assignment add constraint fk_assignment_schoolClass_1 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_assignment_schoolClass_1 on assignment (school_class_id);
alter table note add constraint fk_note_schoolClass_2 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_note_schoolClass_2 on note (school_class_id);
alter table student add constraint fk_student_parent_3 foreign key (parent_id) references parent (id) on delete restrict on update restrict;
create index ix_student_parent_3 on student (parent_id);



alter table school_class_student add constraint fk_school_class_student_schoo_01 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;

alter table school_class_student add constraint fk_school_class_student_stude_02 foreign key (student_id) references student (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists assignment;

drop table if exists note;

drop table if exists parent;

drop table if exists school_class;

drop table if exists school_class_student;

drop table if exists student;

drop table if exists teacher;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists assignment_seq;

drop sequence if exists note_seq;

drop sequence if exists school_class_seq;

