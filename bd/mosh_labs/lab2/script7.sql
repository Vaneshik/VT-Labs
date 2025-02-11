SELECT ИД, ИМЯ
FROM Н_ЛЮДИ
WHERE Н_ЛЮДИ.ИМЯ IN (
    SELECT Н_ЛЮДИ.ИМЯ
    FROM Н_ЛЮДИ
    INNER JOIN Н_УЧЕНИКИ ON Н_ЛЮДИ.ИД = Н_УЧЕНИКИ.ЧЛВК_ИД
    GROUP BY Н_ЛЮДИ.ИМЯ
    HAVING COUNT(DISTINCT Н_ЛЮДИ.ИД)>1
);

