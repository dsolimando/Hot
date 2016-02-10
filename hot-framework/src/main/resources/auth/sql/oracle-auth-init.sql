create table users(
    username varchar2(50) not null primary key,
    password varchar2(50) not null,
    enabled number(1) not null
);

create table authorities (
    username varchar2(50) not null,
    authority varchar2(50) not null,
    constraint fk_authorities_users foreign key(username) references users(username)
);