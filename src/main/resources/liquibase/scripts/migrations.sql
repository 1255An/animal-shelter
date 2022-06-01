-- liquibase formatted sql

-- changeset Diana:1
create table Client
(

    id           serial       NOT NULL PRIMARY KEY,
    chat_id      bigint       NOT NULL,
    name         varchar(255) NOT NULL,
    phone_number varchar(255) NOT NULL,
    email        varchar(255) NOT NULL,
    status       varchar(255) NOT NULL DEFAULT 'USER',
    has_dog      boolean NOT NULL DEFAULT false,
    has_cat      boolean NOT NULL DEFAULT false
);

-- changeset Diana:2
CREATE INDEX user_data_idx ON Client (user) WHERE status = 'USER';

