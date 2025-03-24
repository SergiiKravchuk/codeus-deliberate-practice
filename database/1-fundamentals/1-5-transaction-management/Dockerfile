FROM postgres:latest

COPY src/test/resources/schema.sql /docker-entrypoint-initdb.d/
COPY src/test/resources/test-data.sql /docker-entrypoint-initdb.d/

ENV POSTGRES_USER=postgres
ENV POSTGRES_DB=bank_db
ENV POSTGRES_HOST_AUTH_METHOD=trust

EXPOSE 5432
