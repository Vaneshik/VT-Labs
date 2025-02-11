SELECT ФАМИЛИЯ,
       COUNT(*) AS Количество
FROM Н_ЛЮДИ
WHERE ФАМИЛИЯ IN(
SELECT ФАМИЛИЯ
FROM Н_ЛЮДИ
WHERE ФАМИЛИЯ IN (
SELECT ФАМИЛИЯ
FROM Н_ЛЮДИ
INNER JOIN Н_УЧЕНИКИ ON Н_ЛЮДИ.ИД = Н_УЧЕНИКИ.ЧЛВК_ИД
INNER JOIN Н_ПЛАНЫ ON Н_УЧЕНИКИ.ПЛАН_ИД = Н_ПЛАНЫ.ИД
INNER JOIN Н_ОТДЕЛЫ ON Н_ПЛАНЫ.ОТД_ИД = Н_ОТДЕЛЫ.ИД
WHERE Н_ОТДЕЛЫ.КОРОТКОЕ_ИМЯ = 'КТиУ'
GROUP BY ФАМИЛИЯ
HAVING COUNT(DISTINCT Н_ЛЮДИ.ИД) = 50
   AND COUNT(*) = 50
    	 
) AND ИД	 NOT IN (
SELECT Н_ЛЮДИ.ИД 
FROM Н_ЛЮДИ
INNER JOIN Н_УЧЕНИКИ ON Н_ЛЮДИ.ИД = Н_УЧЕНИКИ.ЧЛВК_ИД)
GROUP BY ФАМИЛИЯ)
GROUP BY ФАМИЛИЯ;
