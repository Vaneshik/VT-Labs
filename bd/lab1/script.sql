CREATE TABLE persons (
    person_id SERIAL PRIMARY KEY,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64),
    is_male BOOLEAN NOT NULL,
    height DECIMAL(5, 2),
    weight DECIMAL(5, 2),
    home_planet_id INTEGER REFERENCES space_objects(opbject_id),
    current_planet_id INTEGER REFERENCES space_objects(object_id)
);

CREATE TABLE space_objects (
    object_id SERIAL PRIMARY KEY,
    coords POINT NOT NULL,
    weight DOUBLE PRECISION,
    diameter DOUBLE PRECISION,
    has_atmosphere BOOLEAN,
    has_magnetic_field BOOLEAN,
);

CREATE TABLE space_objects_names (
    name_id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    is_official BOOLEAN,
    creator_id INTEGER REFERENCES persons(person_id),
    object_id INTEGER REFERENCES space_objects(object_id)
);

CREATE TABLE name_ratings (
    rating_id SERIAL PRIMARY KEY,
    rating NUMERIC(1, 0) NOT NULL,
    rater_id INTEGER REFERENCES persons(person_id),
    name_id INTEGER REFERENCES space_objects_names(name_id)
);

CREATE TABLE emotions (
    emotion_id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    power NUMERIC(1, 0) NOT NULL,
    duration INTERVAL NOT NULL,
    person_id INTEGER REFERENCES persons(person_id)
);

CREATE TABLE nationality (
    nationality_id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    need_visa BOOLEAN NOT NULL,
    multiple_citizenship BOOLEAN NOT NULL
);  

CREATE TABLE person_nationality_relation (
    person_id INTEGER REFERENCES persons(person_id),
    nationality_id INTEGER REFERENCES nationality(nationality_id),
);

INSERT INTO space_objects (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(0, 0), 59742000000000000000, 12742000, TRUE, TRUE);
INSERT INTO space_objects (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(123.23, 35.56), 734767309000000, 123456, TRUE, TRUE);
INSERT INTO space_objects (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(1234.213, 23432.122), 123456, 12324355, FALSE, FALSE);

INSERT INTO persons (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ("Яхве", NULL, TRUE, NULL, NULL, 1, 1);

INSERT INTO space_objects_names (name, is_official, creator_id, object_id) VALUES ("Земля", TRUE, 1, 1);
INSERT INTO space_objects_names (name, is_official, creator_id, object_id) VALUES ("Луна", TRUE, 1, 2);
INSERT INTO space_objects_names (name, is_official, creator_id, object_id) VALUES ("ЛМА-2", TRUE, 2, 3);
INSERT INTO space_objects_names (name, is_official, creator_id, object_id) VALUES ("Загадка", TRUE, 3, 3);

INSERT INTO name_ratings (rating, rater_id, name_id) VALUES (5, 1, 1);
INSERT INTO name_ratings (rating, rater_id, name_id) VALUES (7, 1, 2);
INSERT INTO name_ratings (rating, rater_id, name_id) VALUES (3, 1, 2);
INSERT INTO name_ratings (rating, rater_id, name_id) VALUES (8, 1, 2);

INSERT INTO persons (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ("Дэвид", "Боумен", TRUE, 180, 80, 1, 3);
INSERT INTO persons (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ("Иван", "Иванов", TRUE, 220, 140, 1, 3);

INSERT INTO emotions (name, power, duration, person_id) VALUES ("Хохот", 8, "1 hour", 2);
INSERT INTO emotions (name, power, duration, person_id) VALUES ("Негодование", 5, "128 hours", 3);