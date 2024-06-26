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
export JAVA_TOOL_OPTIONS="--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED \
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
--add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.time=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED \
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"

./gradlew clean build
./gradlew bootRun
./gradlew bootJar
```

To run many node instances

```bash
cd build/libs
java -jar project91-1.0.0.jar --server.port=8081 --ignite.nodeName=node1
java -jar project91-1.0.0.jar --server.port=8082 --ignite.nodeName=node2
java -jar project91-1.0.0.jar --server.port=8083 --ignite.nodeName=node3

```

JVM tuning parameters

```bash
java -jar -Xms1024m -Xmx2048m -XX:MaxDirectMemorySize=256m -XX:+DisableExplicitGC -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:+AlwaysPreTouch project91-1.0.0.jar --server.port=8080 --ignite.nodeName=node0
```
