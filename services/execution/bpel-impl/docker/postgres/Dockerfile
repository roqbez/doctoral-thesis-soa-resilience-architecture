FROM postgres:10.3-alpine

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

ENV POSTGRES_DB="executionservice"
ENV POSTGRES_USER="executionservice"
ENV POSTGRES_PASSWORD="executionservice"

EXPOSE 5432