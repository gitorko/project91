# Project 91

Spring Boot - Apache Ignite

[https://gitorko.github.io/spring-boot-apache-ignite/](https://gitorko.github.io/spring-boot-apache-ignite/)

### Version

Check version

```bash
$java --version
openjdk 21.0.3 2024-04-16 LTS
```

### Postgres DB

```
docker run -p 5432:5432 --name pg-container -e POSTGRES_PASSWORD=password -d postgres:9.6.10
docker ps
docker exec -it pg-container psql -U postgres -W postgres
CREATE USER test WITH PASSWORD 'test@123';
CREATE DATABASE "test-db" WITH OWNER "test" ENCODING UTF8 TEMPLATE template0;
grant all PRIVILEGES ON DATABASE "test-db" to test;

docker stop pg-container
docker start pg-container
```

### Dev

To run the code.

```bash
./gradlew clean build
./gradlew bootRun
```
