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
