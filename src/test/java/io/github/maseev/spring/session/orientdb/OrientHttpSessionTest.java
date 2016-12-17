package io.github.maseev.spring.session.orientdb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import io.github.maseev.spring.session.orientdb.integration.TestConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class OrientHttpSessionTest {

  @Autowired
  private SessionRepository<OrientHttpSession> repository;

  @Test
  public void getIdMustReturnDifferentIdsForDifferentSessionObjects() {
    final OrientHttpSession firstSession = repository.createSession();
    final OrientHttpSession secondSession = repository.createSession();

    assertThat(firstSession.getId(), is(not(equalTo(secondSession.getId()))));
  }

  @Test
  public void isExpiredMustReturnFalseIfMaxInactiveIntervalIsNegative() {
    final OrientHttpSession session = repository.createSession();

    assertThat(session.isExpired(), is(false));
  }

  @Test
  public void isExpiredMustReturnTrueIfSessionHasNotBeeenAccessedLongEnough() {
    final OrientHttpSession session = repository.createSession();

    session.setLastAccessedTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));

    assertThat(session.isExpired(), is(true));
  }

  @Test
  public void getAttributeMustReturnTheValueThatWasPutEarlier() {
    final String key = "key";
    final String value = "value";
    final OrientHttpSession session = repository.createSession();

    session.setAttribute(key, value);

    assertThat(session.getAttribute(key), is(equalTo(value)));
  }

  @Test
  public void passingANullAttributeMustRemoveItCompletely() {
    final String key = "key";
    final String value = "value";
    final OrientHttpSession session = repository.createSession();

    session.setAttribute(key, value);
    session.setAttribute(key, null);

    assertThat(session.getAttribute(key), is(equalTo(null)));
  }

  @Test
  public void afterSettingNegativeValueForMaxInactiveIntervalSessionMustBecomeValidAllTheTime() {
    final OrientHttpSession session = repository.createSession();

    session.setMaxInactiveIntervalInSeconds(-1);
    session.setLastAccessedTime(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));

    assertThat(session.isExpired(), is(equalTo(false)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nonSerializableClassesAreNotAllowed() {
    class NonSerializable {
    }

    final OrientHttpSession session = repository.createSession();

    session.setAttribute("nonserializable", new NonSerializable());
  }
}
