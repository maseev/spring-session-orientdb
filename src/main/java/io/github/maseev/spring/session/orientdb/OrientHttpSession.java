package io.github.maseev.spring.session.orientdb;

import com.orientechnologies.orient.core.id.ORID;

import org.springframework.session.ExpiringSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.persistence.Id;

public class OrientHttpSession implements ExpiringSession {

  public static final String SESSION_ID_PROPERTY = "sessionId";

  public static final String LAST_ACCESSED_TIME_PROPERTY = "lastAccessedTime";

  public static final String MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY =
    "maxInactiveIntervalInSeconds";

  @Id
  private ORID orid;

  private String sessionId;

  private Map<String, Object> attributes;

  private long creationTime;

  private long lastAccessedTime;

  private int maxInactiveIntervalInSeconds;

  public OrientHttpSession() {
    attributes = new HashMap<>();
  }

  public OrientHttpSession(final int maxInactiveIntervalInSeconds) {
    this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    sessionId = UUID.randomUUID().toString();
    attributes = new HashMap<>();
    creationTime = System.currentTimeMillis();
    lastAccessedTime = creationTime;
  }

  @Override
  public String getId() {
    return sessionId;
  }

  @Override
  public void setLastAccessedTime(long lastAccessedTime) {
    this.lastAccessedTime = lastAccessedTime;
  }

  @Override
  public long getLastAccessedTime() {
    return lastAccessedTime;
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public void setMaxInactiveIntervalInSeconds(int interval) {
    maxInactiveIntervalInSeconds = interval;
  }

  @Override
  public int getMaxInactiveIntervalInSeconds() {
    return maxInactiveIntervalInSeconds;
  }

  @Override
  public boolean isExpired() {
    if (maxInactiveIntervalInSeconds < 0) {
      return false;
    }

    final long now = System.currentTimeMillis();

    return now - lastAccessedTime >= TimeUnit.SECONDS.toMillis(maxInactiveIntervalInSeconds);
  }

  @Override
  public Object getAttribute(String attributeName) {
    return attributes.get(attributeName);
  }

  @Override
  public Set<String> getAttributeNames() {
    return attributes.keySet();
  }

  @Override
  public void setAttribute(String attributeName, Object attributeValue) {
    if (attributeValue == null) {
      removeAttribute(attributeName);
    } else {
      attributes.put(attributeName, attributeValue);
    }
  }

  @Override
  public void removeAttribute(String attributeName) {
    attributes.remove(attributeName);
  }
}