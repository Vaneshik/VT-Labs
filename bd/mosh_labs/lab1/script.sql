CREATE TABLE laboratory(
address VARCHAR(100) NOT NULL,
license_number INTEGER CHECK(license_number > 0) PRIMARY KEY,
laboratory_title VARCHAR(50) NOT NULL);


CREATE TABLE research_area(
id SERIAL PRIMARY KEY,
research_area_title VARCHAR(50) NOT NULL UNIQUE);

CREATE TABLE research_area_of_laboratory(
id SERIAL PRIMARY KEY,
research_area_id INTEGER NOT NULL references research_area(id),
laboratory_license_number INTEGER NOT NULL references laboratory(license_number));

CREATE TABLE quality(
id SERIAL PRIMARY KEY,
title TEXT NOT NULL,
description TEXT NOT NULL,
numeric_value INTEGER NOT NULL,
measurement_date DATE NOT NULL);

CREATE TABLE bio_material(
id SERIAL PRIMARY KEY,
amount INTEGER NOT NULL,
quality_id INTEGER NOT NULL references quality(id),
bio_material_name VARCHAR(20) NOT NULL);

CREATE TABLE analyzes(
id SERIAL PRIMARY KEY,
laboratory_license_number INTEGER NOT NULL references laboratory(license_number),
bio_material_id INTEGER NOT NULL references bio_material(id),
patient_initials VARCHAR(100) NOT NULL,
study_duration INTERVAL NOT NULL);

CREATE TABLE serotonin(
id SERIAL PRIMARY KEY,
amount INTEGER NOT NULL);

CREATE TABLE protein(
id SERIAL PRIMARY KEY,
is_big BOOLEAN NOT NULL DEFAULT FALSE,
is_poisonous BOOLEAN NOT NULL DEFAULT FALSE,
weight INTEGER NOT NULL);

CREATE TABLE protein_analyzes(
id SERIAL PRIMARY KEY,
analyzes_id INTEGER NOT NULL references analyzes(id),
protein_id INTEGER NOT NULL references protein(id));

CREATE TABLE biological_activity(
id SERIAL PRIMARY KEY,
is_studied BOOLEAN NOT NULL DEFAULT FALSE,
features TEXT);

CREATE TABLE biological_activity_of_proteins(
id SERIAL PRIMARY KEY,
protein_id INTEGER NOT NULL references protein(id),
biological_activity_id INTEGER NOT NULL references biological_activity(id));

INSERT INTO laboratory(address, license_number, laboratory_title) VALUES('Сан Хосе', 200, 'ИК'),('Av', 201, 'BR'), ('as', 202, 'be');

INSERT INTO research_area(research_area_title) VALUES('раковые клетки'),('слюна знаменитостей'),('слезы студентов ИТМО');

INSERT INTO research_area_of_laboratory(research_area_id, laboratory_license_number) VALUES(3,200);

INSERT INTO quality(title, description, numeric_value, measurement_date) VALUES ('Высокое', 'Материал в идеальном состоянии', 100, '2024-01-21'),('Низкое', 'Материал в плохом состоянии', 20, '2024-01-21'),('Не пригоден', 'Материал в состоянии, не пригодном для исследования', 1, '2024-01-21');

INSERT INTO bio_material(amount, quality_id, bio_material_name) VALUES(100, 1, 'слюна'),(20, 1, 'кровь'), (30, 2, 'blood');

INSERT INTO analyzes(laboratory_license_number, bio_material_id, patient_initials, study_duration) VALUES(200,1,'Тина Боуман', '2 days'), (201, 2, 'Ahley', '2 days'), (201, 3, 'Ahley', '2 days');

INSERT INTO serotonin(amount) VALUES(100);

INSERT INTO protein(is_big, is_poisonous, weight) VALUES (true, true, 100), (true, true, 20), (false, true, 10), (false, false, 300);

INSERT INTO protein_analyzes(analyzes_id, protein_id) VALUES (1, 1), (1,2), (2,3), (3,4);

INSERT INTO biological_activity(is_studied, features) VALUES (true, 'Очень медленная');

INSERT INTO biological_activity_of_proteins(protein_id, biological_activity_id) VALUES (1, 1);

SELECT  *  FROM laboratory;
SELECT  *  FROM research_area_of_laboratory;
SELECT  *  FROM research_area;
SELECT  *  FROM analyzes;
SELECT  *  FROM serotonin;
SELECT  *  FROM bio_material;
SELECT  *  FROM quality;
SELECT  *  FROM protein_analyzes;
SELECT  *  FROM protein;
SELECT  *  FROM biological_activity_of_proteins;
SELECT  *  FROM biological_activity;


