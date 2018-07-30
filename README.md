Spring Session OrientDB
=====================
[![Build Status](https://travis-ci.org/maseev/spring-session-orientdb.svg?branch=master)](https://travis-ci.org/maseev/spring-session-orientdb)
[![Coverage Status](https://coveralls.io/repos/github/maseev/spring-session-orientdb/badge.svg?branch=master)](https://coveralls.io/github/maseev/spring-session-orientdb?branch=master)

[Spring Session](https://github.com/spring-projects/spring-session) Extension
-----------------------------

Spring Session OrientDB is a [Spring Session](https://github.com/spring-projects/spring-session) 
extension which uses [OrientDB](https://github.com/orientechnologies/orientdb) as a session 
storage.

How to build
-----------
* Clone this repository
* Run ``` ./gradlew clean install ``` in the project folder to build the project and install it to the local
Maven repository

How to use
---------
##### Maven
```xml
<dependency>
    <groupId>io.github.maseev</groupId>
    <artifactId>spring-session-orientdb</artifactId>
    <version>1.0</version>
</dependency>
```

##### Gradle
```groovy
compile group: 'io.github.maseev', name: 'spring-session-orientdb', version: '1.0'
```

Add `@EnableOrientHttpSession` annotation to your Spring Boot application class:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.maseev.spring.session.orientdb.EnableOrientHttpSession;

@EnableOrientHttpSession
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

```

Don't forget to specify the following properties:

- **session.db.url** - JDBC URL to the OrientDB database (e.g. `session.db.url=remote:localhost:2424/test`)
- **session.db.username** - username (e.g. `session.db.username=root`)
- **session.db.password** - password (e.g. `session.db.password=root`)
- **session.timeout** - the maximum inactive interval in seconds between requests before a session 
will be invalidated. A negative time indicates that the session will never timeout. (e.g. `session.timeout=60`)
