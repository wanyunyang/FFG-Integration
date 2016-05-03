# --- !Ups

alter table video add public_access tinyint(1) default 0;

# --- !Downs

alter table video drop public_access;
