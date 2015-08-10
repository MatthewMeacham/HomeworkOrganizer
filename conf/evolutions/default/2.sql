# --- !Ups

alter table assignment add column misc varchar(1);

# --- !Downs

alter table assignment drop column misc;