-- file used just for local profile to create a schema ILA_OWNER
CREATE SCHEMA IF NOT EXISTS BGOS_OWNER;
CREATE ROLE IF NOT EXISTS ROLE_BGOS_OWNER;
SET SCHEMA BGOS_OWNER;
CREATE TABLE IF NOT EXISTS shedlock(
                         name VARCHAR(64) NOT NULL,
                         lock_until TIMESTAMP NOT NULL,
                         locked_at TIMESTAMP NOT NULL,
                         locked_by VARCHAR(255) NOT NULL,
                         PRIMARY KEY (name)
);