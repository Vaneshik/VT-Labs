	SELECT EXISTS (
	SELECT Н_ЛЮДИ.ДАТА_РОЖДЕНИЯ, Н_УЧЕНИКИ.ГРУППА
	FROM Н_ЛЮДИ
	JOIN Н_УЧЕНИКИ ON Н_ЛЮДИ.ИД = Н_УЧЕНИКИ.ЧЛВК_ИД
	WHERE Н_УЧЕНИКИ.ГРУППА = '3102' AND
	(CURRENT_DATE < Н_ЛЮДИ.ДАТА_СМЕРТИ AND 
	Н_ЛЮДИ.ДАТА_РОЖДЕНИЯ < CURRENT_DATE - INTERVAL '25 years')
) AS "25 лет";
