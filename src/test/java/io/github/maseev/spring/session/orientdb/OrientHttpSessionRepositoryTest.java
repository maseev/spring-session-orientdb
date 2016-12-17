package io.github.maseev.spring.session.orientdb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import io.github.maseev.spring.session.orientdb.entity.Person;
import io.github.maseev.spring.session.orientdb.integration.TestConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class OrientHttpSessionRepositoryTest {

  @Autowired
  private SessionRepository<OrientHttpSession> repository;

  @Autowired
  private OPartitionedDatabasePool pool;

  @After
  public void after() {
    final String DELETE_ALL_SESSIONS_QUERY =
      "DELETE FROM " + OrientHttpSession.class.getSimpleName();

    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      db.command(new OCommandSQL(DELETE_ALL_SESSIONS_QUERY)).execute();
    }
  }

  @Test
  public void createSessionMustPersistSessionToTheDatabase() {
    final OrientHttpSession session = repository.createSession();
    final OrientHttpSession persistedSession = repository.getSession(session.getId());

    assertThat(persistedSession.getId(), is(equalTo(session.getId())));
    assertThat(persistedSession.getCreationTime(), is(equalTo(session.getCreationTime())));
    assertThat(persistedSession.getMaxInactiveIntervalInSeconds(),
      is(equalTo(session.getMaxInactiveIntervalInSeconds())));
    assertThat(persistedSession.getAttributeNames(), is(equalTo(session.getAttributeNames())));
  }

  @Test
  public void deleteMustRemoveSessionFromDatabase() {
    final OrientHttpSession session = repository.createSession();

    repository.delete(session.getId());

    assertThat(repository.getSession(session.getId()), is(equalTo(null)));
  }

  @Test
  public void getSessionMustDeleteSessionIfItIsExpired() {
    final OrientHttpSession session = repository.createSession();

    session.setLastAccessedTime(session.getLastAccessedTime() - TimeUnit.MINUTES.toMillis(1));
    repository.save(session);

    assertThat(repository.getSession(session.getId()), is(equalTo(null)));
  }

  @Test
  public void flushExpiredSessionsMustDeleteAllExpiredSessions() {
    final OrientHttpSession session = repository.createSession();

    session.setLastAccessedTime(session.getLastAccessedTime() - TimeUnit.MINUTES.toMillis(1));
    repository.save(session);
    ((OrientHttpSessionRepository)repository).flushExpiredSessions();

    assertThat(repository.getSession(session.getId()), is(equalTo(null)));
  }

  @Test
  public void savingASerializableEntityShouldPass() {
    final Person person = new Person("John Doe", 26);
    final OrientHttpSession session = repository.createSession();

    session.setAttribute("person", person);

    repository.save(session);

    final OrientHttpSession savedSession = repository.getSession(session.getId());

    assertThat(savedSession.getAttribute("person"), is(equalTo(person)));
  }
}
