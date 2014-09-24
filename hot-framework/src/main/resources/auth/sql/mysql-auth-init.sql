CREATE TABLE IF NOT EXISTS users(
	username varchar(50) NOT NULL PRIMARY KEY,
	password varchar(50) NOT NULL,
	enabled boolean not null) engine = InnoDb;

CREATE TABLE IF NOT EXISTS authorities (
	username varchar(50) NOT NULL,
	authority varchar(50) NOT NULL,
	foreign key (username) references users(username)) engine = InnoDb;
	
CREATE UNIQUE INDEX ix_auth_username on authorities (username,authority);