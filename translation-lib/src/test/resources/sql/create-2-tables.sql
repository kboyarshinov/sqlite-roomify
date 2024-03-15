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

CREATE INDEX t1_t_index ON t1 (t);

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

CREATE UNIQUE INDEX t2_t_index ON t2 (i1, date);
