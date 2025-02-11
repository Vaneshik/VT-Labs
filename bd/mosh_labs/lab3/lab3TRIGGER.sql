create function quality_checker() returns trigger
    language plpgsql
as
$$
BEGIN
    UPDATE protein SET weight = 0 WHERE protein.id IN (
                SELECT protein.id FROM protein
                JOIN protein_analyzes ON protein.id = protein_analyzes.protein_id
                JOIN analyzes ON protein_analyzes.analyzes_id = analyzes.id
                JOIN bio_material ON analyzes.bio_material_id = bio_material.id
                JOIN quality ON bio_material.quality_id = quality.id
                WHERE quality.numeric_value < 50 AND quality.id =  NEW.quality_id );
    RETURN NEW;
END
$$;

CREATE CONSTRAINT TRIGGER quality_trigger AFTER UPDATE OF quality_id ON bio_material
FOR EACH ROW
    EXECUTE PROCEDURE quality_checker();
