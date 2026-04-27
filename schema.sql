create database examguard;
use examguard;
create table students(
	ID integer primary key,
    name varchar(20) not null,
    password varchar(20) not null,
    class integer
);

create table teachers(
	ID integer primary key,
    name varchar(20) not null,
    password varchar(20) not null,
    subject varchar(10),
    phone integer
);

create table marks(
	name varchar(20) not null,
    ID integer primary key,
    class integer,
    marks float,
    status boolean
);
