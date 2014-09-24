CREATE TABLE users(
	username varchar_ignorecase(50) NOT NULL PRIMARY KEY,
	password varchar_ignorecase(50) NOT NULL,
	enabled boolean not null);

CREATE TABLE authorities (
	username varchar_ignorecase(50) NOT NULL,
	authority varchar_ignorecase(50) NOT NULL,
	CONSTRAINT fk_authorities_users foreign key(username) references users(username));
	CREATE UNIQUE INDEX ix_auth_username on authorities (username,authority);