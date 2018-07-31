package io.github.maseev.spring.session.orientdb;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

@Configuration
@EnableScheduling
@EnableSpringHttpSession
public class OrientSessionConfiguration {

  @Bean
  public OPartitionedDatabasePool db(@Value("${session.db.url}") final String dbUrl,
                                     @Value("${session.db.username}") final String username,
                                     @Value("${session.db.password}") final String password) {
    return new OPartitionedDatabasePool(dbUrl, username, password);
  }

  @Bean
  public SessionRepository<OrientHttpSession> repository(
    final OPartitionedDatabasePool pool, @Value("${session.timeout}") final int sessionTimeout) {
    return new OrientHttpSessionRepository(pool, sessionTimeout);
  }

  @Bean
  public SessionRemover deadSessionRemover(@Value("${session.timeout}") final int sessionTimeout,
                                           SessionRepository<OrientHttpSession> repository) {
    if (sessionTimeout < 0) {
      return new NoOpSessionRemover();
    }

    return new DeadSessionRemover((OrientHttpSessionRepository) repository);
  }
}
