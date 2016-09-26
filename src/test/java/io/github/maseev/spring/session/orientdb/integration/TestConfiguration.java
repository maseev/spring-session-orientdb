package io.github.maseev.spring.session.orientdb.integration;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.exception.OStorageExistsException;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import io.github.maseev.spring.session.orientdb.EnableOrientHttpSession;

@Configuration
@EnableOrientHttpSession
public class TestConfiguration {

  @Bean
  public OPartitionedDatabasePool db(@Value("${session.db.url}") final String dbUrl,
                                     @Value("${session.db.username}") final String username,
                                     @Value("${session.db.password}") final String password) {
    try {
      new OObjectDatabaseTx(dbUrl).create();
    } catch (OStorageExistsException ex) {
      // database already exists. drop it and create another one.
      new OObjectDatabaseTx(dbUrl).open(username, password).drop();
      new OObjectDatabaseTx(dbUrl).create().close();
    }

    return new OPartitionedDatabasePool(dbUrl, username, password);
  }

  @Bean
  public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
    final PropertyPlaceholderConfigurer propertyPlaceholderConfigurer =
      new PropertyPlaceholderConfigurer();
    propertyPlaceholderConfigurer.setLocation(new ClassPathResource("db.properties"));

    return propertyPlaceholderConfigurer;
  }
}
