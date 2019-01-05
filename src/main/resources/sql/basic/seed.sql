insert into users(username, name, given_name, family_name, email, passhash, authorized) values ('keefe','keefe r', 'keefe', 'r', 'keefe@categorize.us', '52ff134d27822c6f83ea898eb2f8ab8ca6507080de32926041f3ed42827d4cfa', true);
insert into users(username, name, given_name, family_name, email, passhash, authorized) values ('keefe1','keefe1 r', 'keefe1', 'r', 'keefe+1@categorize.us', '52ff134d27822c6f83ea898eb2f8ab8ca6507080de32926041f3ed42827d4cfa', true);
insert into users(username, name, given_name, family_name, email, passhash, authorized) values ('keefe2','keefe2 r', 'keefe2', 'r', 'keefe+2@categorize.us', '52ff134d27822c6f83ea898eb2f8ab8ca6507080de32926041f3ed42827d4cfa', true);
insert into users(username, name, given_name, family_name, email, passhash, authorized) values ('keefe3','keefe3 r', 'keefe3', 'r', 'keefe+3@categorize.us', '52ff134d27822c6f83ea898eb2f8ab8ca6507080de32926041f3ed42827d4cfa', true);
insert into users(username, name, given_name, family_name, email, passhash, authorized) values ('keefe4','keefe4 r', 'keefe4', 'r', 'keefe+4@categorize.us', '52ff134d27822c6f83ea898eb2f8ab8ca6507080de32926041f3ed42827d4cfa', true);

insert into tags(tag) values ('tag1');
insert into tags(tag) values ('tag2');
insert into tags(tag) values ('tag3');
insert into tags(tag) values ('tag4');
insert into tags(tag) values ('tag5');
insert into tags(tag) values ('tag6');
insert into tags(tag) values ('tag7');


insert into messages(body, title, posted_by) values ('Here is the body1', 'Thread:Here is the title1', 1);/*1*/
insert into messages(body, title, posted_by) values ('Here is the body2', 'Here is the title2', 2);/*2*/
insert into messages(body, title, posted_by) values ('Here is the body3', 'Here is the title3', 3);/*3*/
insert into messages(body, title, posted_by) values ('Here is the body4', 'Here is the title4', 4);/*4*/
insert into messages(body, title, posted_by) values ('Here is the body5', 'Here is the title5', 3);/*5*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply One', 'Reply One Title', 3,1, 1);/*6*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply to Reply One 1', 'Reply To Reply One 1 Title', 3,6,1);/*7*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply to Reply One 2', 'Reply To Reply One 2 Title', 3, 6,1);/*8*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply to Reply One 3', 'Reply To Reply One 3 Title', 3,6,1);/*9*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply to (Reply to Reply One 1) 1', 'Reply* Title', 3, 7,1);/*10*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply to (Reply to Reply One 1) 2', 'Reply* Title', 3, 7,1);/*11*/
insert into messages(body, title, posted_by, replies_to, root_replies_to) values ('Reply to (Reply to (Reply to Reply One 1) 2) 1', 'Reply* Title', 3, 11,1);/*12*/


insert into message_tags(message_id, tag_id, user_id) values (1, 7, 1);
insert into message_tags(message_id, tag_id, user_id) values (2, 7, 1);
insert into message_tags(message_id, tag_id, user_id) values (3, 7, 1);
insert into message_tags(message_id, tag_id, user_id) values (4, 7, 1);
insert into message_tags(message_id, tag_id, user_id) values (5, 7, 1);
insert into message_tags(message_id, tag_id, user_id) values (1, 1, 1);
insert into message_tags(message_id, tag_id, user_id) values (6, 1, 1);
insert into message_tags(message_id, tag_id, user_id) values (1, 2, 1);
insert into message_tags(message_id, tag_id, user_id) values (1, 3, 1);
insert into message_tags(message_id, tag_id, user_id) values (2, 1, 1);
insert into message_tags(message_id, tag_id, user_id) values (2, 2, 1);
