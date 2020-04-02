drop index if exists messagetag;
drop index if exists user_messages;
drop index if exists message_replies;
drop index if exists message_attachments_message;
drop index if exists message_attachments_attachment;
drop index if exists message_root_replies;
drop index if exists taglookup;
drop index if exists message_attachments_both;

drop table if exists tags;
drop table if exists messages;
drop table if exists users;
drop table if exists message_tags;
drop table if exists user_sessions;
drop table if exists attachments;
drop table if exists message_attachments;
drop table if exists attachment_signatures;


create table if not exists tags(
	id bigserial primary key,
	tag varchar(255) not null
);

create table if not exists messages(
	id bigserial primary key,
	body text,
	title text,
	posted_by bigint,
	replies_to bigint,
	root_replies_to bigint
);
create table if not exists message_attachments(
	message_id bigint,
	attachment_id bigint
);
create table if not exists attachments(
	id bigserial primary key,
	filename text,
	extension text,
	length bigint
);
create table if not exists attachment_signatures(
	attachment_id bigint,
	signature text
);

create table if not exists users(
	id bigserial primary key,
	username text,
	email text,
	name text,
	given_name text,
	family_name text,
	authorized boolean default false,
	passhash text
);

create table if not exists message_tags(
	message_id bigint,
	tag_id bigint,
	user_id bigint
);

create table if not exists user_sessions(
	session_uuid varchar(128) primary key,
	user_id bigint
);

create unique index messagetag on message_tags(message_id, tag_id);
create index user_messages on messages(posted_by);
create index message_replies on messages(replies_to);
/*todo review these indices*/
create unique index message_attachments_both on message_attachments(message_id, attachment_id);
create index message_attachments_message on message_attachments(message_id);
create index message_attachments_attachment on message_attachments(attachment_id);
create index message_root_replies on messages(root_replies_to);
create unique index taglookup on tags(tag);
#  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO categorizeus;
#  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO categorizeus;

