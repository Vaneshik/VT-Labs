DELETE FROM persons;
DELETE FROM locations;

ALTER SEQUENCE persons_id_seq RESTART WITH 1;
ALTER SEQUENCE locations_id_seq RESTART WITH 1;

-- Добавление чистых тестовых данных
INSERT INTO locations (x, y, z, name) VALUES 
    (100, 200, 300, 'Moscow'),
    (150, 250, 350, 'Saint Petersburg'),
    (200, 300, 400, 'Berlin'),
    (250, 350, 450, 'Paris'),
    (300, 400, 500, 'Madrid');

INSERT INTO persons (name, x, y, creation_date, eye_color, hair_color, location_id, height, weight, passport_id, nationality) 
VALUES 
    ('Ivan Petrov', 100, 200, NOW(), 'BLUE', 'BLACK', 1, 180, 75, 'RU1234567', 'GERMANY'),
    ('Maria Smirnova', 200, 300, NOW(), 'GREEN', 'BROWN', 1, 165, 60, 'RU2345678', 'FRANCE'),
    ('John Smith', 300, 400, NOW(), 'BROWN', 'BLUE', 3, 175, 80, 'US1234567', 'GERMANY'),
    ('Anna Mueller', 150, 250, NOW(), 'BLUE', 'WHITE', 3, 170, 65, 'DE1234567', 'GERMANY'),
    ('Pierre Dubois', 250, 350, NOW(), 'BLACK', 'BLACK', 4, 178, 78, 'FR1234567', 'FRANCE'),
    ('Carlos Garcia', 350, 450, NOW(), 'BROWN', 'BROWN', 5, 172, 70, 'ES1234567', 'SPAIN'),
    ('Elena Ivanova', 120, 220, NOW(), 'GREEN', 'BROWN', 2, 168, 62, 'RU3456789', 'FRANCE'),
    ('Hans Schmidt', 280, 380, NOW(), 'BLUE', 'WHITE', 3, 182, 85, 'DE2345678', 'GERMANY'),
    ('Marie Laurent', 320, 420, NOW(), 'GREEN', 'BLACK', 4, 163, 58, 'FR2345678', 'FRANCE'),
    ('Miguel Rodriguez', 180, 280, NOW(), 'BROWN', 'BROWN', 5, 176, 73, 'ES2345678', 'SPAIN'),
    ('Olga Kozlova', 140, 240, NOW(), 'BLUE', NULL, 2, 171, 67, 'RU4567890', 'GERMANY'),
    ('Francesco Rossi', 260, 360, NOW(), 'BLACK', 'BLACK', NULL, 169, 72, 'IT1234567', 'VATICAN'),
    ('Sofia Gonzalez', 340, 440, NOW(), 'BROWN', 'BROWN', 5, 160, 55, 'ES3456789', 'SPAIN'),
    ('Thomas Müller', 190, 290, NOW(), 'BLUE', 'WHITE', 3, 185, 88, 'DE3456789', 'GERMANY'),
    ('Isabella Romano', 270, 370, NOW(), 'GREEN', NULL, NULL, 166, 61, 'IT2345678', 'VATICAN');

SELECT COUNT(*) as total_persons FROM persons;
SELECT COUNT(*) as total_locations FROM locations;

SELECT 
    nationality,
    COUNT(*) as count,
    AVG(height) as avg_height,
    AVG(weight) as avg_weight
FROM persons
WHERE nationality IS NOT NULL
GROUP BY nationality
ORDER BY count DESC;

