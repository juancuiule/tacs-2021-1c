CREATE TABLE if not exists users
(
    id           bigserial PRIMARY KEY,
    userName     varchar NOT NULL UNIQUE,
    passwordHash varchar NOT NULL,
    role         varchar NOT NULL DEFAULT 'Player'
);

CREATE TABLE if not exists stat
(
    id           integer,
    height       integer NOT NULL,
    weight       integer NOT NULL,
    intelligence integer NOT NULL,
    speed        integer NOT NULL,
    power        integer NOT NULL,
    combat       integer NOT NULL,
    strength     integer NOT NULL
);

alter table stat add constraint stat_id primary key (id);

CREATE TABLE if not exists biography
(
    id        integer,
    fullName  varchar,
    publisher varchar
);

alter table biography add constraint biography_id primary key (id);

CREATE TABLE if not exists card
(
    id        integer,
    name      varchar NOT NULL,
    image     varchar NOT NULL
);

alter table card add constraint card_id primary key (id);