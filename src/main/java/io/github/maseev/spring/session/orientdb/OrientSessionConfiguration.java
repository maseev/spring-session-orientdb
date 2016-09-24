package io.github.maseev.spring.session.orientdb;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.web.http.SessionRepositoryFilter;

import javax.servlet.ServletContext;

@Configuration
@EnableScheduling
public class OrientSessionConfiguration {

  @Bean
  public SessionRepositoryFilter<OrientHttpSession> springSessionRepositoryFilter(
    final OrientHttpSessionRepository repository, final ServletContext servletContext) {
    final SessionRepositoryFilter<OrientHttpSession> filter =
      new SessionRepositoryFilter<>(repository);
    filter.setServletContext(servletContext);

    return filter;
  }

  @Bean
  public OrientHttpSessionRepository orientHttpSessionRepository(
    @Value("${session.db.url}") final String dbUrl,
    @Value("${session.db.username}") final String username,
    @Value("${session.db.password}") final String password,
    @Value("${session.timeout}") final int sessionTimeout) {
    final OObjectDatabaseTx db = new OObjectDatabaseTx(dbUrl).open(username, password);
    return new OrientHttpSessionRepository(db, sessionTimeout);
  }
}
