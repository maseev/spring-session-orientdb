package io.github.maseev.spring.session.orientdb;

import com.orientechnologies.orient.core.id.ORID;

import org.springframework.session.ExpiringSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.persistence.Id;
import javax.persistence.Transient;

public class OrientHttpSession implements ExpiringSession {

  public static final String SESSION_ID_PROPERTY = "sessionId";

  public static final String LAST_ACCESSED_TIME_PROPERTY = "lastAccessedTime";

  public static final String MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY =
    "maxInactiveIntervalInSeconds";

  private static final byte[] ZERO_LENGTH_ARRAY = new byte[0];

  @Id
  private ORID orid;

  private String sessionId;

  private long creationTime;

  private long lastAccessedTime;

  private int maxInactiveIntervalInSeconds;

  private byte[] serializedAttributes;

  @Transient
  private Map<String, Serializable> attributes;

  public OrientHttpSession() {
    serializedAttributes = ZERO_LENGTH_ARRAY;
    attributes = new HashMap<>();
  }

  public OrientHttpSession(final int maxInactiveIntervalInSeconds) {
    this();
    this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    sessionId = UUID.randomUUID().toString();
    creationTime = System.currentTimeMillis();
    lastAccessedTime = creationTime;
  }

  @Override
  public String getId() {
    return sessionId;
  }

  @Override
  public void setLastAccessedTime(final long lastAccessedTime) {
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
  public void setMaxInactiveIntervalInSeconds(final int interval) {
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
  @SuppressWarnings("unchecked")
  public Object getAttribute(final String attributeName) {
    return attributes.get(attributeName);
  }

  @Override
  public Set<String> getAttributeNames() {
    return attributes.keySet();
  }

  @Override
  public void setAttribute(final String attributeName, final Object attributeValue) {
    if (attributeValue == null) {
      removeAttribute(attributeName);
    } else {
      if (attributeValue instanceof Serializable) {
        attributes.put(attributeName, (Serializable) attributeValue);
      } else {
        final String msg = "%s must implement %s in order to be saved to the database";
        throw new IllegalArgumentException(
          String.format(msg, attributeValue.getClass(), Serializable.class));
      }
    }
  }

  @Override
  public void removeAttribute(final String attributeName) {
    attributes.remove(attributeName);
  }

  void serializeAttributes() {
    if (attributes.isEmpty()) {
      return;
    }

    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
         final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(attributes);
      serializedAttributes = bos.toByteArray();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @SuppressWarnings("unchecked")
  void deserializeAttributes() {
    if (serializedAttributes.length == 0) {
      return;
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(serializedAttributes);
         final ObjectInputStream ois = new ObjectInputStream(bis)) {
      attributes = (Map<String, Serializable>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
