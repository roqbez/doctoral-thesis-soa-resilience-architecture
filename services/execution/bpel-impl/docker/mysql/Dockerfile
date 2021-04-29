FROM mysql/mysql-server:5.7

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

ENV MYSQL_DATABASE="executionservice"
ENV MYSQL_USER="executionservice"
ENV MYSQL_PASSWORD="executionservice"
ENV MYSQL_ROOT_HOST="%"
ENV MYSQL_ROOT_PASSWORD="admin"

EXPOSE 3306