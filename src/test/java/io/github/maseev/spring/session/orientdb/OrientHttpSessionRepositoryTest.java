package io.github.maseev.spring.session.orientdb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class OrientHttpSessionRepositoryTest {

  private OrientHttpSessionRepository repository;

  private OObjectDatabaseTx db;

  @Before
  public void before() {
    new OObjectDatabaseTx("memory:tmpdb").create().close();
    db = new OObjectDatabaseTx("memory:tmpdb").open("admin", "admin");
    repository = new OrientHttpSessionRepository(db, 60);
  }

  @After
  public void after() {
    db.drop();
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
    repository.flushExpiredSessions();

    assertThat(repository.getSession(session.getId()), is(equalTo(null)));
  }
}
