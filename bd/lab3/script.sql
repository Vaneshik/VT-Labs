DROP TABLE IF EXISTS person CASCADE;
DROP TABLE IF EXISTS emotion CASCADE;
DROP TABLE IF EXISTS space_object CASCADE;
DROP TABLE IF EXISTS space_object_name CASCADE;
DROP TABLE IF EXISTS name_rating CASCADE;
DROP TABLE IF EXISTS citizenship CASCADE;
DROP TABLE IF EXISTS person_citizenship_relation CASCADE;
DROP TABLE IF EXISTS top_name_raters CASCADE;

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

-- Триггер для обновления топа оценщиков и добавления эмоций
CREATE TABLE top_name_raters (
    person_id INT PRIMARY KEY,
    rating_count INT NOT NULL
);

CREATE OR REPLACE FUNCTION update_top_name_raters() 
RETURNS TRIGGER AS $$
DECLARE
    current_count INT;
    new_top_count INT;
    person_id_to_delete INT;
BEGIN
    -- Увеличиваем счетчик оценок для человека
    SELECT COUNT(*) INTO current_count FROM name_rating WHERE person_id = NEW.person_id;
    
    -- Если человек уже в топе, обновляем его счетчик
    IF EXISTS (SELECT 1 FROM top_name_raters WHERE person_id = NEW.person_id) THEN
        UPDATE top_name_raters SET rating_count = current_count WHERE person_id = NEW.person_id;
    ELSE
        -- Вставляем новый счетчик, если человека нет в топе
        INSERT INTO top_name_raters (person_id, rating_count) VALUES (NEW.person_id, current_count);
        INSERT INTO emotion (name, duration, power, person_id) VALUES ('Счастье', INTERVAL '1 day', 5, NEW.person_id);
        RAISE NOTICE 'Человек с id=% добавлен в топ-10, он счастлив 1 день', NEW.person_id;
    END IF;
    
    -- Получаем айди человека, который вылетает из топа
    SELECT person_id FROM top_name_raters ORDER BY (rating_count, person_id) DESC OFFSET 11 LIMIT 1 INTO person_id_to_delete;
    
    -- Если человек существует, то удаляем его
    IF person_id_to_delete IS NOT NULL THEN
        RAISE NOTICE 'Человек с id=% удален из топа, он впал в тильт на 7 дней', person_id_to_delete;
        RAISE NOTICE ' ';
        DELETE FROM top_name_raters WHERE person_id = person_id_to_delete;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_top_name_raters
AFTER INSERT ON name_rating
FOR EACH ROW
EXECUTE FUNCTION update_top_name_raters();


-- Тестовые данные
INSERT INTO space_object (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(0, 0), 59742000000000000000, 12742000, TRUE, TRUE), (POINT(123.23, 35.56), 734767309000000, 123456, TRUE, TRUE), (POINT(1234.213, 23432.122), 123456, 12324355, FALSE, FALSE);
INSERT INTO person (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ('Яхве', 'Фрикович', TRUE, NULL, NULL, 1, 1);

INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('Земля', TRUE, 1, 1), ('Луна', TRUE, 1, 2);
INSERT INTO name_rating (rating, person_id, name_id) VALUES (5, 1, 1), (7, 1, 2);

INSERT INTO person (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ('Дэвид', 'Боумен', TRUE, 180, 80, 1, 3), ('Иван', 'Иванов', TRUE, 220, 140, 1, 3);
INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('ЛМА-2', TRUE, 2, 3), ('Загадка', TRUE, 3, 3);
INSERT INTO name_rating (rating, person_id, name_id) VALUES (3, 2, 3), (8, 3, 4);

INSERT INTO emotion (name, power, duration, person_id) VALUES ('Хохот', 4, '1 hour', 2), ('Негодование', 5, '128 hours', 3);
INSERT INTO citizenship (name, need_visa) VALUES ('Россия', FALSE), ('Америка', TRUE);
INSERT INTO person_citizenship_relation (person_id, citizenship_id) VALUES (1, 1), (2, 1), (3, 1);


-- Проверка триггера
INSERT INTO person (first_name, last_name, is_male, height, weight, home_planet_id, current_planet_id) VALUES ('Александр', 'Пушкин', TRUE, 180, 80, 1, 3),
('Сергей', 'Сергеев', TRUE, 220, 140, 1, 3), ('Андрей', 'Андреев', TRUE, 220, 140, 1, 3),
('Михаил', 'Михайлов', TRUE, 220, 140, 1, 3), ('Владимир', 'Владимиров', TRUE, 220, 140, 1, 3),
('Алексей', 'Алексеев', TRUE, 220, 140, 1, 3), ('Дмитрий', 'Дмитриев', TRUE, 220, 140, 1, 3),
('Николай', 'Николаев', TRUE, 220, 140, 1, 3), ('Антон', 'Антонов', TRUE, 220, 140, 1, 3),
('Василий', 'Васильев', TRUE, 220, 140, 1, 3), ('Игорь', 'Игорев', TRUE, 220, 140, 1, 3),
('Семен', 'Семенов', TRUE, 220, 140, 1, 3), ('Артем', 'Артемов', TRUE, 220, 140, 1, 3),
('Аркадий', 'Аркадьев', TRUE, 220, 140, 1, 3), ('Анатолий', 'Анатольев', TRUE, 220, 140, 1, 3),
('Артур', 'Артуров', TRUE, 220, 140, 1, 3), ('Арсений', 'Арсеньев', TRUE, 220, 140, 1, 3),
('Аркадий', 'Аркадьев', TRUE, 220, 140, 1, 3);

INSERT INTO space_object (coords, weight, diameter, has_atmosphere, has_magnetic_field) VALUES (POINT(124, 112), 78327896387578236563872, 2343252, TRUE, TRUE), (POINT(323, 3234), 234567894576, 34234324, TRUE, FALSE), (POINT(1234.213, 23432.122), 123456, 12324355, TRUE, FALSE);
INSERT INTO space_object_name (name, is_official, creator_id, object_id) VALUES ('Марс', TRUE, 2, 4), ('Венера', TRUE, 1, 5), ('3423424', TRUE, 3, 4), ('Мяу', TRUE, 2, 5), ('ЛОЛ134424', TRUE, 1, 4), ('Кек', TRUE, 3, 5);
INSERT INTO name_rating (rating, person_id, name_id) VALUES (9, 1, 2), (8, 2, 1), (7, 3, 3), (6, 4, 4), (5, 5, 5), (4, 6, 6), (3, 7, 1), (2, 8, 2), (1, 9, 3), (0, 10, 4), (9, 11, 5), (8, 12, 6), (7, 13, 1), (6, 14, 2), (5, 15, 3), (4, 16, 4), (3, 17, 5), (2, 18, 6), (1, 19, 1), (0, 20, 2);
