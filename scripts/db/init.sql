CREATE TABLE "user" (
    id         SERIAL PRIMARY KEY,
    username   VARCHAR UNIQUE,
    password   VARCHAR,
    first_name VARCHAR,
    last_name  VARCHAR,
    email      VARCHAR UNIQUE,
    phone_no   VARCHAR UNIQUE,
    avatar     VARCHAR
);

CREATE TABLE device (
    id   SERIAL PRIMARY KEY,
    name VARCHAR,
    imei VARCHAR(50) UNIQUE
);

CREATE TABLE app_role (
    id   SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE
);

CREATE TABLE user_role (
    user_id   INT,
    device_id INT,
    role_id   INT,

    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES "user" (id),
    FOREIGN KEY (role_id) REFERENCES app_role (id)
);

CREATE TABLE token (
    token           UUID PRIMARY KEY,
    user_id         INT,
    expired_instant TIMESTAMP
);

CREATE TABLE otp (

);

CREATE table cell_id (
    radio
        mcc
        net
        area
        cell
        unit
        lon
        lat
        range
        samples
        changeablemesage
        created
        updated
        averageSignal
);
