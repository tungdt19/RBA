SELECT token, user_id, expired_instant, id, username, password, first_name, last_name, email, phone_no
FROM token t
         LEFT JOIN end_user u ON t.user_id = u.id
WHERE (expired_instant IS NULL OR expired_instant > CURRENT_TIMESTAMP) AND t.token = ?;

INSERT INTO test_json (content)
VALUES
    (?::JSONB);

UPDATE test_json
SET
    content = content || '{"name": "name 1", "lat": 123.456789, "lon": 23.1456789}'
WHERE id = 1 AND JSONB_ARRAY_LENGTH(content) < 5;

SELECT content
FROM test_json
WHERE id = 1;

SELECT phone_no
FROM end_user
         JOIN user_role ur ON end_user.id = ur.user_id
         JOIN device d ON d.id = ur.device_id
WHERE platform_device_id = ?;


SELECT phone_no, platform_group_id, d.id, d.name, platform_device_id
FROM user_role ur
         JOIN device d ON ur.device_id = d.id
         JOIN end_user eu ON eu.id = ur.user_id;
