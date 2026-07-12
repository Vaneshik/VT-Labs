CREATE OR REPLACE FUNCTION delete_persons_by_nationality(p_nationality VARCHAR)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM persons WHERE nationality = p_nationality;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_average_height()
RETURNS NUMERIC AS $$
DECLARE
    avg_height NUMERIC;
BEGIN
    SELECT AVG(height) INTO avg_height 
    FROM persons 
    WHERE height IS NOT NULL;
    
    RETURN COALESCE(avg_height, 0);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_unique_nationalities()
RETURNS TABLE(nationality VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT p.nationality
    FROM persons p
    WHERE p.nationality IS NOT NULL
    ORDER BY p.nationality;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_hair_color_percentage(p_hair_color VARCHAR)
RETURNS NUMERIC AS $$
DECLARE
    total_count INTEGER;
    color_count INTEGER;
    percentage NUMERIC;
BEGIN
    SELECT COUNT(*) INTO total_count FROM persons;
    
    IF total_count = 0 THEN
        RETURN 0;
    END IF;
    
    SELECT COUNT(*) INTO color_count 
    FROM persons 
    WHERE haircolor = p_hair_color;
    
    percentage := (color_count::NUMERIC / total_count::NUMERIC) * 100;
    
    RETURN ROUND(percentage, 2);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION count_by_hair_color_and_location(
    p_hair_color VARCHAR,
    p_location_id INTEGER
)
RETURNS INTEGER AS $$
DECLARE
    person_count INTEGER;
BEGIN
    IF p_location_id IS NULL THEN
        SELECT COUNT(*) INTO person_count
        FROM persons
        WHERE haircolor = p_hair_color
        AND location_id IS NULL;
    ELSE
        SELECT COUNT(*) INTO person_count
        FROM persons
        WHERE haircolor = p_hair_color
        AND location_id = p_location_id;
    END IF;
    
    RETURN COALESCE(person_count, 0);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_person_with_details(p_person_id INTEGER)
RETURNS TABLE(
    person_id INTEGER,
    person_name VARCHAR,
    coord_x INTEGER,
    coord_y BIGINT,
    creation_date TIMESTAMP,
    eye_color VARCHAR,
    hair_color VARCHAR,
    location_id INTEGER,
    location_name VARCHAR,
    location_coords VARCHAR,
    height INTEGER,
    weight NUMERIC,
    passport_id VARCHAR,
    nationality VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        p.x AS coord_x,
        p.y AS coord_y,
        p.creationdate,
        p.eyecolor,
        p.haircolor,
        l.id AS location_id,
        l.name AS location_name,
        CONCAT('(', l.x, ', ', l.y, ', ', l.z, ')') AS location_coords,
        p.height,
        p.weight,
        p.passportid,
        p.nationality
    FROM persons p
    LEFT JOIN locations l ON p.location_id = l.id
    WHERE p.id = p_person_id;
END;
$$ LANGUAGE plpgsql;
