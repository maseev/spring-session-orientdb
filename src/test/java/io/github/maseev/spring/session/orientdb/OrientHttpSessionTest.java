package io.github.maseev.spring.session.orientdb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class OrientHttpSessionTest {

  @Test
  public void getIdMustReturnDifferentIdsForDifferentSessionObjects() {
    final int maxInactiveIntervalInSeconds = 30;
    final OrientHttpSession firstSession = new OrientHttpSession(maxInactiveIntervalInSeconds);
    final OrientHttpSession secondSession = new OrientHttpSession(maxInactiveIntervalInSeconds);

    assertThat(firstSession.getId(), is(not(equalTo(secondSession.getId()))));
  }

  @Test
  public void isExpiredMustReturnFalseIfMaxInactiveIntervalIsNegative() {
    final OrientHttpSession session = new OrientHttpSession(-1);

    assertThat(session.isExpired(), is(false));
  }

  @Test
  public void isExpiredMustReturnTrueIfSessionHasNotBeeenAccessedLongEnough() {
    final OrientHttpSession session = new OrientHttpSession(1);

    session.setLastAccessedTime(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1));

    assertThat(session.isExpired(), is(true));
  }

  @Test
  public void getAttributeMustReturnTheValueThatWasPutEarlier() {
    final String key = "key";
    final String value = "value";
    final OrientHttpSession session = new OrientHttpSession(1);

    session.setAttribute(key, value);

    assertThat(session.getAttribute(key), is(equalTo(value)));
  }

  @Test
  public void passingANullAttributeMustRemoveItCompletely() {
    final String key = "key";
    final String value = "value";
    final OrientHttpSession session = new OrientHttpSession(1);

    session.setAttribute(key, value);
    session.setAttribute(key, null);

    assertThat(session.getAttribute(key), is(equalTo(null)));
  }

  @Test
  public void afterSettingNegativeValueForMaxInactiveIntervalSessionMustBecomeValidAllTheTime() {
    final OrientHttpSession session = new OrientHttpSession(1);

    session.setMaxInactiveIntervalInSeconds(-1);
    session.setLastAccessedTime(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));

    assertThat(session.isExpired(), is(equalTo(false)));
  }
}
