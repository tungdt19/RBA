SELECT token, user_id, expired_instant, id, username, password, first_name, last_name, email, phone_no
FROM token t
         LEFT JOIN end_user u ON t.user_id = u.id
WHERE (expired_instant IS NULL OR expired_instant > CURRENT_TIMESTAMP) AND t.token = ?;



SELECT phone_no
FROM end_user
         JOIN user_role ur ON end_user.id = ur.user_id
         JOIN device d ON d.id = ur.device_id
WHERE platform_device_id = ?;



SELECT ur.user_id, phone_no, platform_group_id, d.id, d.name, platform_device_id
FROM user_role ur
         JOIN device d ON ur.device_id = d.id
         JOIN end_user eu ON eu.id = ur.user_id;



SELECT latitude, longitude, trigger_instant
FROM location_history lh
         JOIN user_role ur ON lh.device_id = ur.device_id
         JOIN device d ON d.id = ur.device_id
WHERE platform_device_id = ?
ORDER BY trigger_instant
LIMIT 1 OFFSET 0;



SELECT id, name, imei, platform_device_id, battery
FROM device d
WHERE platform_device_id = ?;



SELECT d.id, name, platform_device_id, d.battery
FROM device d;



SELECT d.id, name, platform_device_id, d.battery, lh.device_id, lh.latitude, lh.longitude, lh.trigger_instant
FROM device d
         JOIN location_history lh ON d.id = lh.device_id
         LEFT OUTER JOIN location_history lh2 ON d.id = lh2.device_id AND (lh.trigger_instant > lh2.trigger_instant
    OR lh.trigger_instant = lh2.trigger_instant)
WHERE lh2.device_id IS NULL;


-- count history
SELECT device_id, COUNT(1) AS history, MIN(trigger_instant), MAX(trigger_instant)
FROM public.location_history
GROUP BY device_id
ORDER BY device_id;


-- select route history
SELECT latitude, longitude, trigger_instant
FROM location_history lh
         JOIN user_role ur ON lh.device_id = ur.device_id
         JOIN device d ON d.id = ur.device_id
WHERE trigger_instant > ? AND trigger_instant < ? AND platform_device_id = ?
ORDER BY trigger_instant;


--- select duplicated device message
SELECT device_id, COUNT(1) AS c, DATE_TRUNC('minute', trigger_instant) AS t, STDDEV_POP(latitude) AS lat,
       STDDEV_POP(longitude) AS lng
FROM location_history
GROUP BY t, device_id
HAVING COUNT(1) > 1
ORDER BY t;


--- delete duplicated device message
DELETE
FROM location_history a USING location_history b
WHERE a.trigger_instant < b.trigger_instant
  AND DATE_TRUNC('minute', a.trigger_instant) = DATE_TRUNC('minute', b.trigger_instant);



SELECT u.id, u.phone_no, u.platform_group_id, user_id, device_id, d.platform_device_id
FROM end_user u
         FULL OUTER JOIN user_role ur ON u.id = ur.user_id
         FULL OUTER JOIN device d ON d.id = ur.device_id;
-- WHERE t2.user_id IS NULL

SELECT id, name, imei, platform_device_id, battery
FROM device d
         JOIN user_role ur ON d.id = ur.device_id
         JOIN location_history lh ON d.id = lh.device_id AND trigger_instant = (
    SELECT device_id, MAX(trigger_instant) AS t FROM location_history GROUP BY device_id
);



SELECT id, name, imei, platform_device_id, battery, latitude, longitude, trigger_instant
FROM device d
         LEFT JOIN location_history lh ON d.id = lh.device_id
         JOIN user_role ur ON d.id = ur.device_id
WHERE platform_device_id = ?
ORDER BY trigger_instant
LIMIT 1 OFFSET 0;

-- uppdate
UPDATE device d
SET
    geo_fencing = geo_fencing || ?::JSONB
FROM user_role ur
WHERE ur.device_id = d.id AND ur.user_id = ? AND platform_device_id = ? AND geo_fencing -> 'name 1' IS NOT NULL;



ALTER TABLE user_role
    DROP CONSTRAINT user_role_user_id_fkey;

ALTER TABLE user_role
    ADD CONSTRAINT user_role_user_id_fkey FOREIGN KEY (user_id) REFERENCES end_user ON DELETE CASCADE;

ALTER TABLE user_role
    DROP CONSTRAINT user_role_device_id_fkey;

ALTER TABLE user_role
    ADD CONSTRAINT user_role_device_id_fkey FOREIGN KEY (device_id) REFERENCES device ON DELETE CASCADE;
