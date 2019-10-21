CREATE TABLE BANK_ACCOUNT_APPLICATIONS (
    ID BIGSERIAL PRIMARY KEY,
    ACCOUNT_TYPE VARCHAR(25) NOT NULL,
    STATUS VARCHAR(25) NOT NULL,
    DATE TIMESTAMP NOT NULL
);

CREATE TABLE BANK_ACCOUNT (
    ID BIGSERIAL PRIMARY KEY,
    IBAN VARCHAR(35) NOT NULL,
    STATUS VARCHAR(25) NOT NULL,
    TYPE VARCHAR(25) NOT NULL,
    DAILY_LIMIT DECIMAL NOT NULL
);

CREATE TABLE BANK_ACCOUNT_TX (
    BANK_ACCOUNT_ID BIGINT REFERENCES BANK_ACCOUNT(ID) DEFERRABLE INITIALLY DEFERRED,
    AMOUNT DECIMAL NOT NULL,
    BOOKING_TIME TIMESTAMP NOT NULL,
    INDEX INTEGER NOT NULL,
    TYPE VARCHAR(25) NOT NULL
);

CREATE TABLE CUSTOMER (
    ID BIGSERIAL PRIMARY KEY,
    FIRST_NAME VARCHAR NOT NULL,
    LAST_NAME VARCHAR NOT NULL,
    EMAIL VARCHAR NOT NULL UNIQUE
);

CREATE TABLE OFFER (
    ID BIGSERIAL PRIMARY KEY,
    STATUS VARCHAR(25) NOT NULL,
    PRICE DECIMAL NOT NULL
);

CREATE TABLE SCHEDULED_COMMAND (
    ID BIGSERIAL PRIMARY KEY,
    RAN_TIMES INTEGER NOT NULL,
    LAST_RUN_TIME TIMESTAMP,
    CREATION_DATE TIMESTAMP NOT NULL,
    STATUS VARCHAR(25) NOT NULL,
    COMMAND VARCHAR NOT NULL
);