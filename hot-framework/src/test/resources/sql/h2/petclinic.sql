CREATE SCHEMA petclinic AUTHORIZATION DBA;
CREATE USER dso PASSWORD dso;

--drop table petclinic.vets;
--drop table petclinic.specialties;
--drop table petclinic.vet_specialties;
--drop table petclinic.types;
--drop table petclinic.owners;
--drop table petclinic.pets;
--drop table petclinic.visits;

--drop index vets_last_name;
--drop index specialties_name;
--drop index types_name;
--drop index owners_last_name;
--drop index pets_name;
--drop index visits_pet_id;

CREATE TABLE petclinic.vets (
	id INTEGER NOT NULL  PRIMARY KEY,
	first_name VARCHAR(30),
	last_name VARCHAR(30)
);
--CREATE INDEX vets_last_name ON petclinic.vets(last_name);

CREATE TABLE petclinic.specialties (
	id INTEGER NOT NULL  PRIMARY KEY,
	name VARCHAR(80)
);
--CREATE INDEX specialties_name ON petclinic.specialties(name);

CREATE TABLE petclinic.vet_specialties (
	vet_id INTEGER NOT NULL,
	specialty_id INTEGER NOT NULL
);
alter table petclinic.vet_specialties add constraint fk_vet_specialties_vets foreign key (vet_id) references petclinic.vets(id);
alter table petclinic.vet_specialties add constraint fk_vet_specialties_specialties foreign key (specialty_id) references petclinic.specialties(id);

CREATE TABLE petclinic.types (
	id INTEGER NOT NULL  PRIMARY KEY,
	name VARCHAR(80)
);
--CREATE INDEX types_name ON petclinic.types(name);

CREATE TABLE petclinic.owners (
	id INTEGER NOT NULL  PRIMARY KEY,
	first_name VARCHAR(30),
	last_name VARCHAR(30),
	address VARCHAR(255),
	city VARCHAR(80),
	telephone VARCHAR(20)
);
--CREATE INDEX owners_last_name ON petclinic.owners(last_name);

CREATE TABLE petclinic.pets (
	id INTEGER NOT NULL  PRIMARY KEY,
	name VARCHAR(30),
	birth_date DATE,
	type_id INTEGER NOT NULL,
	owner_id INTEGER NOT NULL
);
alter table petclinic.pets add constraint fk_pets_owners foreign key (owner_id) references petclinic.owners(id);
alter table petclinic.pets add constraint fk_pets_types foreign key (type_id) references petclinic.types(id);
--CREATE INDEX pets_name ON petclinic.pets(name);

CREATE TABLE petclinic.visits (
	id INTEGER NOT NULL  PRIMARY KEY,
	pet_id INTEGER NOT NULL,
	visit_date DATE,
	description VARCHAR(255)
);
alter table petclinic.visits add constraint fk_visits_pets foreign key (pet_id) references petclinic.pets(id);
--CREATE INDEX visits_pet_id ON petclinic.visits(pet_id);