-- drop all foreign keys
alter table article drop constraint if exists fk_article_category_id;
drop index if exists ix_article_category_id;

-- drop all
drop table if exists article;

drop table if exists category;

drop table if exists url;

