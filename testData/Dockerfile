FROM mysql:latest

ENV MYSQL_DATABASE=matching \
    MYSQL_ROOT_PASSWORD=example

ADD schema.sql /docker-entrypoint-initdb.d

EXPOSE 3306
