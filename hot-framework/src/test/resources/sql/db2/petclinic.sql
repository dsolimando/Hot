CONNECT TO PETCLI;

drop table vets;
drop table specialties;
drop table vet_specialties;
drop table types;
drop table owners;
drop table pets;
drop table visits;

drop index vets_last_name;
drop index types_name;
drop index owners_last_name;
drop index pets_name;
drop index visits_pet_id;

CREATE TABLE vets (
	id INTEGER NOT NULL,
	first_name VARCHAR(30),
	last_name VARCHAR(30),
	PRIMARY KEY(id)
);
CREATE INDEX vets_last_name ON vets(last_name);

CREATE TABLE specialties (
	id INTEGER NOT NULL,
	name VARCHAR(80),
	PRIMARY KEY (id)
);
CREATE INDEX specialties_name ON specialties(name);

CREATE TABLE vet_specialties (
	vet_id INTEGER NOT NULL,
	specialty_id INTEGER NOT NULL
);
alter table vet_specialties add constraint fk_vet_specialties_vets foreign key (vet_id) references vets(id);
alter table vet_specialties add constraint fk_vet_specialties_specialties foreign key (specialty_id) references specialties(id);

CREATE TABLE types (
	id INTEGER NOT NULL,
	name VARCHAR(80),
	PRIMARY KEY(id)
);
CREATE INDEX types_name ON types(name);

CREATE TABLE owners (
	id INTEGER NOT NULL,
	first_name VARCHAR(30),
	last_name VARCHAR(30),
	address VARCHAR(255),
	city VARCHAR(80),
	telephone VARCHAR(20),
	PRIMARY KEY(id)
);
CREATE INDEX owners_last_name ON owners(last_name);

CREATE TABLE pets (
	id INTEGER NOT NULL,
	name VARCHAR(30),
	birth_date DATE,
	type_id INTEGER NOT NULL,
	owner_id INTEGER NOT NULL,
	PRIMARY KEY(id)
);
alter table pets add constraint fk_pets_owners foreign key (owner_id) references owners(id);
alter table pets add constraint fk_pets_types foreign key (type_id) references types(id);
CREATE INDEX pets_name ON pets(name);

CREATE TABLE visits (
	id INTEGER NOT NULL,
	pet_id INTEGER NOT NULL,
	visit_date DATE,
	description VARCHAR(255),
	PRIMARY KEY(id)
);
alter table visits add constraint fk_visits_pets foreign key (pet_id) references pets(id);
CREATE INDEX visits_pet_id ON visits(pet_id);