# preparedQuery-reproducer

I created this repo for the following issue:
https://github.com/eclipse-vertx/vertx-sql-client/issues/1142

This project was generated with a basic vertx application and a single verticle.

Before running this reproducer, please update the MainVerticle static attributes with your PostgreSQL configuration (url, port, username, password etc...).
Your PostgreSQL server must already contains the specified database and table, and the table have to contains a 'name' column (rows will be inserted with 'name' values).

By default 65536 rows will be inserted to raise the error to reproduce, feel free to change the MainVerticle.nbEntities attribute to change this behaviour ( < 65536 the error will not be raised ).

# How to run:
 - ./mvnw clean package
 - java -jar ./target/reproducer-1.0.0-SNAPSHOT-fat.jar
