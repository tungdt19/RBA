CREATE TABLE IF NOT EXISTS end_user (
    id                SERIAL PRIMARY KEY,
    username          VARCHAR UNIQUE,
    password          VARCHAR,
    first_name        VARCHAR,
    last_name         VARCHAR,
    email             VARCHAR UNIQUE,
    phone_no          VARCHAR UNIQUE,
    avatar            VARCHAR,
    fcm_token         VARCHAR,
    platform_group_id UUID -- platform's group ID
);

CREATE TABLE IF NOT EXISTS device (
    id                 SERIAL PRIMARY KEY,
    name               VARCHAR,
    imei               VARCHAR(50) UNIQUE,
    platform_device_id UUID UNIQUE
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
    FOREIGN KEY (role_id) REFERENCES app_role (id)
);

CREATE TABLE IF NOT EXISTS token (
    token           UUID PRIMARY KEY,
    user_id         INT,
    expired_instant TIMESTAMP
);

CREATE TABLE IF NOT EXISTS otp (
    otp             VARCHAR(6),
    phone           VARCHAR(15),
    expired_instant TIMESTAMP,
    PRIMARY KEY (otp, phone, expired_instant)
);

CREATE TABLE IF NOT EXISTS cell_id (
    mcc      INT,
    mnc      INT,
    lac      INT,
    cell_id  INT,
    unit     INT,
    lon      FLOAT8,
    lat      FLOAT8,
    accuracy DECIMAL,
    address  TEXT,
    PRIMARY KEY (mcc, mnc, cell_id, lac)
);

CREATE TABLE location_history (
    device_id       INT,
    latitude        FLOAT8,
    longitude       FLOAT8,
    accuracy        INT,
    insert_instant  TIMESTAMP,
    trigger_instant TIMESTAMP
);


------------------------------------------------------------------------------------------------------------------------
INSERT INTO app_role (id, name)
VALUES
    (1, 'ROLE_OWNER'),
    (2, 'ROLE_VIEWER');

INSERT INTO end_user(phone_no, password)
VALUES
('$2a$10$qh02zcA0vzdFya0EpO59dOrxGwGZwxLgVExg6ZC2tyyu.27izBGbS', '0365731814'),
('$2a$10$jQzz7NLdCAtS6OzlRt6t2uARre5xi/BdwFI8F5a4S30INkWEE0yty', '0869999904')

