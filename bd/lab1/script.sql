DROP TABLE IF EXISTS person CASCADE;
DROP TABLE IF EXISTS emotion CASCADE;
DROP TABLE IF EXISTS space_object CASCADE;
DROP TABLE IF EXISTS space_object_name CASCADE;
DROP TABLE IF EXISTS name_rating CASCADE;
DROP TABLE IF EXISTS citizenship CASCADE;
DROP TABLE IF EXISTS person_citizenship_relation CASCADE;

CREATE TABLE space_object (
    object_id SERIAL PRIMARY KEY,
    diameter DOUBLE PRECISION CHECK(diameter > 0),
    coords POINT,
    has_magnetic_field BOOLEAN,
    has_atmosphere BOOLEAN,
    weight DOUBLE PRECISION CHECK(weight > 0)
);

CREATE TABLE person (
    person_id SERIAL PRIMARY KEY,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    is_male BOOLEAN NOT NULL,
    height DECIMAL(5, 2) CHECK(height > 0 AND height <= 300),
    weight DECIMAL(5, 2) CHECK(weight > 0 AND weight <= 300),
    home_planet_id INTEGER REFERENCES space_object(object_id),
    current_planet_id INTEGER REFERENCES space_object(object_id)
);


CREATE TABLE space_object_name (
    name_id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    is_official BOOLEAN,
    creator_id INTEGER REFERENCES person(person_id),
    object_id INTEGER REFERENCES space_object(object_id) NOT NULL
);

CREATE TABLE name_rating (
    rating_id SERIAL PRIMARY KEY,
    rating NUMERIC(1, 0) NOT NULL CHECK(rating >= 0 AND rating <= 9),
    person_id INTEGER REFERENCES person(person_id),
    name_id INTEGER REFERENCES space_object_name(name_id) NOT NULL
);

CREATE TABLE emotion (
    emotion_id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    power NUMERIC(1, 0) NOT NULL CHECK(power >= 0 AND power <= 5),
    duration INTERVAL NOT NULL,
    person_id INTEGER REFERENCES person(person_id) NOT NULL
);

CREATE TABLE citizenship (
    citizenship_id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    need_visa BOOLEAN
);  

CREATE TABLE person_citizenship_relation (
    person_id INTEGER REFERENCES person(person_id),
    citizenship_id INTEGER REFERENCES citizenship(citizenship_id),
    PRIMARY KEY(person_id, citizenship_id)
);

INSERT INTO space_object (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(0, 0), 59742000000000000000, 12742000, TRUE, TRUE);
INSERT INTO space_object (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(123.23, 35.56), 734767309000000, 123456, TRUE, TRUE);
INSERT INTO space_object (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(1234.213, 23432.122), 123456, 12324355, FALSE, FALSE);

INSERT INTO person (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ('Яхве', 'Фрикович', TRUE, NULL, NULL, 1, 1);

INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('Земля', TRUE, 1, 1);
INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('Луна', TRUE, 1, 2);

INSERT INTO name_rating (rating, person_id, name_id) VALUES (5, 1, 1);
INSERT INTO name_rating (rating, person_id, name_id) VALUES (7, 1, 2);

INSERT INTO person (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ('Дэвид', 'Боумен', TRUE, 180, 80, 1, 3);
INSERT INTO person (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ('Иван', 'Иванов', TRUE, 220, 140, 1, 3);

INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('ЛМА-2', TRUE, 2, 3);
INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('Загадка', TRUE, 3, 3);
INSERT INTO name_rating (rating, person_id, name_id) VALUES (3, 2, 3);
INSERT INTO name_rating (rating, person_id, name_id) VALUES (8, 3, 4);

INSERT INTO emotion (name, power, duration, person_id) VALUES ('Хохот', 4, '1 hour', 2);
INSERT INTO emotion (name, power, duration, person_id) VALUES ('Негодование', 5, '128 hours', 3);

INSERT INTO citizenship (name, need_visa) VALUES ('Россия', FALSE);

INSERT INTO person_citizenship_relation (person_id, citizenship_id) VALUES (1, 1);
INSERT INTO person_citizenship_relation (person_id, citizenship_id) VALUES (2, 1);
INSERT INTO person_citizenship_relation (person_id, citizenship_id) VALUES (3, 1);