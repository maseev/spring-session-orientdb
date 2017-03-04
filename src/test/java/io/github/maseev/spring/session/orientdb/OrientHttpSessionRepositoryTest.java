package io.github.maseev.spring.session.orientdb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.github.maseev.spring.session.orientdb.entity.Person;

public class OrientHttpSessionRepositoryTest extends OrientDbTest {

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
