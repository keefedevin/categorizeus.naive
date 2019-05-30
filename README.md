# categorizeus.naive
naive implementation of the interface defined in categorizeus.core

loading sql file from psql , the first one needs to be run as the psql user on the default db
\i C:/Users/keefe/projects/categorizeus.naive/src/main/resources/sql/postgres/init.sql

now the database and user exists, so we log in with the categorizeus user on the categories database. 

\i C:/Users/keefe/projects/categorizeus.naive/src/main/resources/sql/postgres/schema.sql
\i C:/Users/keefe/projects/categorizeus.naive/src/main/resources/sql/postgres/seed.sql

DON'T FORGET the bloody semicolon! 
drop database "categories;
