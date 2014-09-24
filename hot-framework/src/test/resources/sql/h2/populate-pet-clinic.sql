INSERT INTO petclinic.vets VALUES (1, 'James', 'Carter');
INSERT INTO petclinic.vets VALUES (2, 'Helen', 'Leary');
INSERT INTO petclinic.vets VALUES (3, 'Linda', 'Douglas');
INSERT INTO petclinic.vets VALUES (4, 'Rafael', 'Ortega');
INSERT INTO petclinic.vets VALUES (5, 'Henry', 'Stevens');
INSERT INTO petclinic.vets VALUES (6, 'Sharon', 'Jenkins');

INSERT INTO petclinic.specialties VALUES (1, 'radiology');
INSERT INTO petclinic.specialties VALUES (2, 'surgery');
INSERT INTO petclinic.specialties VALUES (3, 'dentistry');

INSERT INTO petclinic.vet_specialties VALUES (2, 1);
INSERT INTO petclinic.vet_specialties VALUES (3, 2);
INSERT INTO petclinic.vet_specialties VALUES (3, 3);
INSERT INTO petclinic.vet_specialties VALUES (4, 2);
INSERT INTO petclinic.vet_specialties VALUES (5, 1);

INSERT INTO petclinic.types VALUES (1, 'cat');
INSERT INTO petclinic.types VALUES (2, 'dog');
INSERT INTO petclinic.types VALUES (3, 'lizard');
INSERT INTO petclinic.types VALUES (4, 'snake');
INSERT INTO petclinic.types VALUES (5, 'bird');
INSERT INTO petclinic.types VALUES (6, 'hamster');

INSERT INTO petclinic.owners VALUES (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO petclinic.owners VALUES (2, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO petclinic.owners VALUES (3, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO petclinic.owners VALUES (4, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198');
INSERT INTO petclinic.owners VALUES (5, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765');
INSERT INTO petclinic.owners VALUES (6, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO petclinic.owners VALUES (7, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387');
INSERT INTO petclinic.owners VALUES (8, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683');
INSERT INTO petclinic.owners VALUES (9, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435');
INSERT INTO petclinic.owners VALUES (10, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT INTO petclinic.pets VALUES (1, 'Leo', '2000-09-07', 1, 1);
INSERT INTO petclinic.pets VALUES (2, 'Basil', '2002-08-06', 6, 2);
INSERT INTO petclinic.pets VALUES (3, 'Rosy', '2001-04-17', 2, 3);
INSERT INTO petclinic.pets VALUES (4, 'Jewel', '2000-03-07', 2, 3);
INSERT INTO petclinic.pets VALUES (5, 'Iggy', '2000-11-30', 3, 4);
INSERT INTO petclinic.pets VALUES (6, 'George', '2000-01-20', 4, 5);
INSERT INTO petclinic.pets VALUES (7, 'Samantha', '1995-09-04', 1, 6);
INSERT INTO petclinic.pets VALUES (8, 'Max', '1995-09-04', 1, 6);
INSERT INTO petclinic.pets VALUES (9, 'Lucky', '1999-08-06', 5, 7);
INSERT INTO petclinic.pets VALUES (10, 'Mulligan', '1997-02-24', 2, 8);
INSERT INTO petclinic.pets VALUES (11, 'Freddy', '2000-03-09', 5, 9);
INSERT INTO petclinic.pets VALUES (12, 'Lucky', '2000-06-24', 2, 10);
INSERT INTO petclinic.pets VALUES (13, 'Sly', '2002-06-08', 1, 10);

INSERT INTO petclinic.visits VALUES (1, 7, '1996-03-04', 'rabies shot');
INSERT INTO petclinic.visits VALUES (2, 8, '1996-03-04', 'rabies shot');
INSERT INTO petclinic.visits VALUES (3, 8, '1996-06-04', 'neutered');
INSERT INTO petclinic.visits VALUES (4, 7, '1996-09-04', 'spayed');