# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table homework (
  id                        bigint not null,
  due_date                  varchar(255),
  school_class_id           bigint,
  constraint pk_homework primary key (id))
;

create table note (
  id                        bigint not null,
  title                     varchar(255),
  notes                     varchar(255),
  student_email             varchar(255),
  school_class_id           bigint,
  constraint pk_note primary key (id))
;

create table school_class (
  id                        bigint not null,
  subject                   varchar(255),
  student_email             varchar(255),
  constraint pk_school_class primary key (id))
;

create table student (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  grade                     varchar(255),
  constraint pk_student primary key (email))
;

create table teacher (
  id                        bigint not null,
  name                      varchar(255),
  school_class_id           bigint,
  constraint pk_teacher primary key (id))
;

create table test (
  id                        bigint not null,
  date_of                   timestamp,
  content                   varchar(255),
  school_class_id           bigint,
  constraint pk_test primary key (id))
;

create sequence homework_seq;

create sequence note_seq;

create sequence school_class_seq;

create sequence student_seq;

create sequence teacher_seq;

create sequence test_seq;

alter table homework add constraint fk_homework_schoolClass_1 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_homework_schoolClass_1 on homework (school_class_id);
alter table note add constraint fk_note_student_2 foreign key (student_email) references student (email) on delete restrict on update restrict;
create index ix_note_student_2 on note (student_email);
alter table note add constraint fk_note_schoolClass_3 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_note_schoolClass_3 on note (school_class_id);
alter table school_class add constraint fk_school_class_student_4 foreign key (student_email) references student (email) on delete restrict on update restrict;
create index ix_school_class_student_4 on school_class (student_email);
alter table teacher add constraint fk_teacher_schoolClass_5 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_teacher_schoolClass_5 on teacher (school_class_id);
alter table test add constraint fk_test_schoolClass_6 foreign key (school_class_id) references school_class (id) on delete restrict on update restrict;
create index ix_test_schoolClass_6 on test (school_class_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists homework;

drop table if exists note;

drop table if exists school_class;

drop table if exists student;

drop table if exists teacher;

drop table if exists test;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists homework_seq;

drop sequence if exists note_seq;

drop sequence if exists school_class_seq;

drop sequence if exists student_seq;

drop sequence if exists teacher_seq;

drop sequence if exists test_seq;

