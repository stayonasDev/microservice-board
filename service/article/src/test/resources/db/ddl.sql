create database article;
use article;

create table article(
    article_id bigint not null primary key,
    title varchar(100) not null,
    content varchar(3000) not null,
    board_id bigint not null, -- Shard Key
    writer_id bigint not null,
    created_at datetime not null,
    modified_at datetime not null
);