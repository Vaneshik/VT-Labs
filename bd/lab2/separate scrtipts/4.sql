SELECT "Н_ГРУППЫ_ПЛАНОВ"."ПЛАН_ИД"
FROM "Н_ГРУППЫ_ПЛАНОВ"
WHERE "ПЛАН_ИД" IN (SELECT "Н_ПЛАНЫ"."ИД"
                    FROM "Н_ПЛАНЫ"
                             INNER JOIN "Н_ОТДЕЛЫ" ON "Н_ОТДЕЛЫ"."ИД" = "Н_ПЛАНЫ"."ОТД_ИД"
                    WHERE "Н_ОТДЕЛЫ"."КОРОТКОЕ_ИМЯ" = 'КТиУ')
GROUP BY "Н_ГРУППЫ_ПЛАНОВ"."ПЛАН_ИД"
HAVING COUNT(*) > 2;