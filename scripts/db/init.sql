CREATE TABLE IF NOT EXISTS end_user (
    id                SERIAL PRIMARY KEY,
    username          VARCHAR UNIQUE,
    phone_no          VARCHAR UNIQUE,
    first_name        VARCHAR,
    last_name         VARCHAR,
    email             VARCHAR UNIQUE,
    avatar            VARCHAR,
    password          VARCHAR,
    fcm_token         VARCHAR,
    platform_group_id UUID UNIQUE -- platform's group ID
);

CREATE TABLE IF NOT EXISTS device (
    id                 SERIAL PRIMARY KEY,
    name               VARCHAR,
    imei               VARCHAR(50) UNIQUE,
    platform_device_id UUID UNIQUE,
    battery            INT,
    status             INT,
    geo_status         BOOLEAN,
    geo_length         INT,
    geo_fencing        JSONB,
    last_lat           FLOAT8,
    last_lon           FLOAT8,
    accuracy           INT,
    update_instant     TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS app_role (
    id   SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id   INT,
    device_id INT,
    role_id   INT,

    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES end_user (id),
    FOREIGN KEY (device_id) REFERENCES device (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS token (
    token           UUID PRIMARY KEY,
    user_id         INT,
    expired_instant TIMESTAMP
);

CREATE TYPE OTP_TYPE AS ENUM ( 'TYPE_CREATE_USER', 'TYPE_RESET_PASSWORD' );

CREATE TABLE IF NOT EXISTS otp (
    otp             VARCHAR(6),
    phone           VARCHAR(15),
    expired_instant TIMESTAMP,
    otp_type        OTP_TYPE,
    PRIMARY KEY (otp, phone, expired_instant)
);

CREATE TABLE IF NOT EXISTS location_history (
    device_id       INT,
    latitude        FLOAT8,
    longitude       FLOAT8,
    accuracy        INT,
    trigger_instant TIMESTAMP
);


CREATE TABLE IF NOT EXISTS device_history (
    device_id       INT,
    platform_id     UUID,
    trigger_instant TIMESTAMP
);

------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_device_location_function() RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    INSERT INTO device(last_lat, last_lon, accuracy) VALUES (new.lat, new.lon, new.accuracy);
    RETURN new;
END;
$$;

CREATE TRIGGER update_device_location_trigger
    BEFORE INSERT OR UPDATE
    ON device FROM location_history INITIALLY DEFERRED --     [ FOR [ EACH ] { ROW | STATEMENT } ]
--     [ WHEN ( condition ) ]
EXECUTE PROCEDURE update_device_location_function(arguments)


------------------------------------------------------------------------------------------------------------------------
INSERT INTO app_role (id, name)
VALUES
    (1, 'ROLE_OWNER'),
    (2, 'ROLE_VIEWER');


