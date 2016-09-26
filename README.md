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
* Just run ``` ./gradlew clean install ``` to build the project and install it to the local
 Maven repository

How to use
---------

Make sure that your project meets the following dependencies (use the recommended versions or 
above):

* [orientdb-core:2.2.10](https://mvnrepository.com/artifact/com.orientechnologies/orientdb-core/2.2.10)
* [orientdb-client:2.2.10](https://mvnrepository.com/artifact/com.orientechnologies/orientdb-client/2.2.10)
* [orientdb-object:2.2.10](https://mvnrepository.com/artifact/com.orientechnologies/orientdb-object/2.2.10)
* [spring-session:1.2.2.RELEASE](https://mvnrepository.com/artifact/org.springframework.session/spring-session/1.2.2.RELEASE)
* [spring-context:4.3.3.RELEASE](https://mvnrepository.com/artifact/org.springframework/spring-context/4.3.3.RELEASE)
* [spring-webmvc:4.3.3.RELEASE](https://mvnrepository.com/artifact/org.springframework/spring-webmvc/4.3.3.RELEASE)
* [javax.servlet-api:3.1.0](https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api/3.1.0)

Add `spring-session-orientdb` as a dependency:

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

Add `@EnableOrientHttpSession` annotation to your Spring configuration class:

```java
import org.springframework.context.annotation.Configuration;
import io.github.maseev.spring.session.orientdb.EnableOrientHttpSession;

@Configuration
@EnableOrientHttpSession
public class MyConfiguration {
  
  // Your spring beans configuration goes here
}
```

And don't forget to add the following properties:

- **session.db.url** - JDBC URL to the OrientDB database
- **session.db.username** - username
- **session.db.password** - password
- **session.timeout** - the maximum inactive interval in seconds between requests before a session 
will be invalidated. A negative time indicates that the session will never timeout.

How to contribute
---------------
Found a bug or have an idea how to improve this project? Don't hesitate to open a new issue in the issue tracker or send a PR. I :heart: PRs.