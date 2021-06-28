-- GET ACTIVE TOKEN
SELECT token, user_id, expired_instant, id, username, password, first_name, last_name, email, phone_no
FROM token t
         LEFT JOIN end_user u ON t.user_id = u.id
WHERE (expired_instant IS NULL OR expired_instant > CURRENT_TIMESTAMP) AND t.token = ?;



SELECT phone_no,
FROM end_user
         JOIN user_role ur ON end_user.id = ur.user_id
         JOIN device d ON d.id = ur.device_id
-- WHERE platform_device_id = ?
;



SELECT ur.user_id, phone_no, platform_group_id, d.id AS d_id, d.name, platform_device_id -- fcm_token
FROM user_role ur
         FULL OUTER JOIN device d ON ur.device_id = d.id
         FULL OUTER JOIN end_user eu ON eu.id = ur.user_id
-- WHERE platform_device_id = ?
;



SELECT latitude, longitude, trigger_instant
FROM location_history lh
         JOIN user_role ur ON lh.device_id = ur.device_id
         JOIN device d ON d.id = ur.device_id
     -- WHERE platform_device_id = ?
ORDER BY trigger_instant
-- LIMIT 1 OFFSET 0
;



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
ORDER BY t DESC;

SELECT COUNT(*)
FROM location_history a
         JOIN location_history b ON a.device_id = b.device_id AND a.trigger_instant < b.trigger_instant
    AND DATE_TRUNC('minute', a.trigger_instant) = DATE_TRUNC('minute', b.trigger_instant);

--- delete duplicated device message
DELETE
FROM location_history a USING location_history b
WHERE a.device_id = b.device_id
  AND a.trigger_instant < b.trigger_instant
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

-- update
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
------------------------------------------------------------------------------------------------------------------------

SELECT TO_CHAR(NOW(), 'YYYY_MM');

SELECT device_id, COUNT(0) AS gps_count
FROM location_history
WHERE trigger_instant > '2021-05-05' AND trigger_instant < '2021-05-05 23:59:59.999999' AND accuracy IS NULL
GROUP BY device_id;


INSERT INTO device (name, platform_device_id)
VALUES
    ('test', 'd7834a67-9e72-4d66-92e3-7ffc567165e1');

DELETE
FROM device
WHERE platform_device_id = 'd7834a67-9e72-4d66-92e3-7ffc567165e1';

-- CREATE TABLE IF NOT EXISTS test_partition_2021_03 PARTITION OF test_partition FOR VALUES FROM ('2021-03-01') TO ('2021-04-01');
-- CREATE INDEX test_partition_2021_03_log_time ON test_partition_2021_03(log_time);


-- @formatter:off
DO $$
DECLARE
    _table CONSTANT VARCHAR = 'test_partition';
    _index CONSTANT VARCHAR = 'id';
    _field CONSTANT VARCHAR = 'log_time';
    date_begin CONSTANT DATE = '2000-01-01';
    date_end CONSTANT DATE = '2100-01-01';
    create_query TEXT;
    index_query  TEXT;
BEGIN
    FOR create_query, index_query IN SELECT
        'CREATE TABLE IF NOT EXISTS ' || _table || '_' || TO_CHAR(d, 'YYYY_MM') || ' PARTITION OF ' || _table
            || ' FOR VALUES FROM (''' || d || ''') TO (''' || d + INTERVAL '1 month' || ''');',
        'CREATE INDEX IF NOT EXISTS ' || _table || '_' || TO_CHAR(d, 'YYYY_MM') || '_' || _field || ' ON ' || _table
            || '_' || TO_CHAR(d, 'YYYY_MM') || '(' || _field || ' DESC, ' || _index || ' ASC);'
    FROM GENERATE_SERIES(date_begin, date_end, INTERVAL '1 month') AS d
        LOOP
            RAISE NOTICE '%', create_query;
            EXECUTE create_query;

            RAISE NOTICE '%', index_query;
            EXECUTE index_query;
            RAISE NOTICE '--------------------------------------------------------------------------------';
        END LOOP;
END; $$ LANGUAGE plpgsql;
-- @formatter:on

SELECT *
FROM device
WHERE id NOT IN (300, 343, 294, 291, 355, 165, 166, 170, 325, 288, 322, 324, 342, 349, 319, 348)
