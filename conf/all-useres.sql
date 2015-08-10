CREATE VIEW `test_view` AS SELECT * FROM 
(
	SELECT id, name, email FROM parent
	UNION ALL
    SELECT id, name, email FROM student
    UNION ALL 
    SELECT id, name, email FROM teacher
) users
