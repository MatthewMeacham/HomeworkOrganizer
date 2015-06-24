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
  constraint pk_assignment primary key (id))
;

create table note (
  id                        bigint not null,
  title                     varchar(255),
  notes                     varchar(255),
  student_id                bigint,
  school_class_id           bigint,
  constraint pk_note primary key (id))
;

create table parent (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  constraint pk_parent primary key (email))
;

create table school_class (
  id                        bigint not null,
  subject                   varchar(255),
  student_id                bigint,
  constraint pk_school_class primary key (id))
;

create table student (
  id                        bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  password                  varchar(255),
  grade                     varchar(255),
  parent_email              varchar(255),
  constraint pk_student primary key (id))
;

create table teacher (
  id                        bigint not null,
  name                      varchar(255),
  school_class_id           bigint,
  constraint pk_teacher primary key (id))
;

create sequence assignment_seq;

create sequence note_seq;

create sequence parent_seq;

create sequence school_class_seq;

create sequence student_seq;

create sequence teacher_seq;

alter table assignment add constraint fk_assignment_schoolClass_1 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_assignment_schoolClass_1 on assignment (school_class_id);
alter table note add constraint fk_note_student_2 foreign key (student_id) references student (id) on delete restrict on update restrict;
create index ix_note_student_2 on note (student_id);
alter table note add constraint fk_note_schoolClass_3 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_note_schoolClass_3 on note (school_class_id);
alter table school_class add constraint fk_school_class_student_4 foreign key (student_id) references student (id) on delete restrict on update restrict;
create index ix_school_class_student_4 on school_class (student_id);
alter table student add constraint fk_student_parent_5 foreign key (parent_email) references parent (email) on delete restrict on update restrict;
create index ix_student_parent_5 on student (parent_email);
alter table teacher add constraint fk_teacher_schoolClass_6 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_teacher_schoolClass_6 on teacher (school_class_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists assignment;

drop table if exists note;

drop table if exists parent;

drop table if exists school_class;

drop table if exists student;

drop table if exists teacher;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists assignment_seq;

drop sequence if exists note_seq;

drop sequence if exists parent_seq;

drop sequence if exists school_class_seq;

drop sequence if exists student_seq;

drop sequence if exists teacher_seq;

