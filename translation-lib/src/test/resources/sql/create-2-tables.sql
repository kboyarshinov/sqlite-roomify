CREATE TABLE t1(
    id      INT NOT NULL PRIMARY KEY,
    t       TEXT NOT NULL,
    nu      NUMERIC,
    i       INTEGER,
    i1      INT,
    ti      TINYINT,
    bi      BIGINT,
    r       REAL,
    d       DOUBLE,
    f       FLOAT,
    blob    BLOB,
    b       BOOLEAN,
    date    DATE,
    dt      DATETIME
);

CREATE TABLE t2(
    t       TEXT NOT NULL,
    nu      NUMERIC,
    i       INTEGER,
    i1      INT,
    ti      TINYINT,
    bi      BIGINT,
    r       REAL,
    d       DOUBLE,
    f       FLOAT,
    blob    BLOB,
    b       BOOLEAN,
    date    DATE,
    dt      DATETIME
);
