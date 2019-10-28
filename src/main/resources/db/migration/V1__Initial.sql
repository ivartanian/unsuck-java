CREATE TABLE BANK_ACCOUNT_APPLICATION (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ACCOUNT_TYPE VARCHAR(25) NOT NULL,
    STATUS VARCHAR(25) NOT NULL,
    DATE TIMESTAMP NOT NULL
);

CREATE TABLE BANK_ACCOUNT (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    IBAN VARCHAR(35) NOT NULL,
    STATUS VARCHAR(25) NOT NULL,
    TYPE VARCHAR(25) NOT NULL,
    DAILY_LIMIT DECIMAL(13, 2) NOT NULL
);

CREATE TABLE BANK_ACCOUNT_TX (
    BANK_ACCOUNT_ID BIGINT REFERENCES BANK_ACCOUNT(ID) DEFERRABLE INITIALLY DEFERRED,
    INDEX INTEGER GENERATED ALWAYS AS IDENTITY,
    AMOUNT DECIMAL(13, 2) NOT NULL,
    BOOKING_TIME TIMESTAMP NOT NULL,
    TYPE VARCHAR(25) NOT NULL,
    PRIMARY KEY(BANK_ACCOUNT_ID, INDEX)
);

CREATE TABLE CUSTOMER (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    FIRST_NAME VARCHAR NOT NULL,
    LAST_NAME VARCHAR NOT NULL,
    EMAIL VARCHAR NOT NULL UNIQUE
);

CREATE TABLE OFFER (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    STATUS VARCHAR(25) NOT NULL,
    PRICE DECIMAL(13, 2) NOT NULL
);

CREATE TABLE SCHEDULED_COMMAND (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    CREATION_DATE TIMESTAMP NOT NULL,
    COMMAND VARCHAR NOT NULL,
    COMMAND_ID VARCHAR NOT NULL
);