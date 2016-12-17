package io.github.maseev.spring.session.orientdb;

import static com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE.NOTUNIQUE;
import static com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE.UNIQUE_HASH_INDEX;
import static io.github.maseev.spring.session.orientdb.OrientHttpSession.LAST_ACCESSED_TIME_PROPERTY;
import static io.github.maseev.spring.session.orientdb.OrientHttpSession.MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY;
import static io.github.maseev.spring.session.orientdb.OrientHttpSession.SESSION_ID_PROPERTY;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.SessionRepository;

import java.util.List;

import javax.annotation.PreDestroy;

public class OrientHttpSessionRepository implements SessionRepository<OrientHttpSession> {

  private static final String ENTITY_NAME = OrientHttpSession.class.getSimpleName();

  private static final OSQLSynchQuery<OrientHttpSession> FIND_BY_ID_QUERY =
    new OSQLSynchQuery<>("SELECT * FROM " + ENTITY_NAME + " WHERE " + SESSION_ID_PROPERTY + " = ?");

  private static final OCommandSQL DELETE_BY_ID_QUERY =
    new OCommandSQL("DELETE FROM " + ENTITY_NAME + " WHERE " + SESSION_ID_PROPERTY + " = ?");

  private static final OCommandSQL DELETE_EXPIRED_SESSIONS_QUERY =
    new OCommandSQL("DELETE FROM " + ENTITY_NAME
      + " WHERE " + MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY + " > 0 "
      + " AND date().asLong() - " + LAST_ACCESSED_TIME_PROPERTY + " >= "
      + MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY + " * " + "1000");

  private final OPartitionedDatabasePool pool;

  private final int sessionTimeout;

  public OrientHttpSessionRepository(final OPartitionedDatabasePool pool,
                                     final int sessionTimeout) {
    this.pool = pool;
    this.sessionTimeout = sessionTimeout;

    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      db.setAutomaticSchemaGeneration(true);
      db.getEntityManager().registerEntityClass(OrientHttpSession.class);
      db.getMetadata().getSchema().synchronizeSchema();

      final OClass sessionClass = db.getMetadata().getSchema().getClass(OrientHttpSession.class);

      createIndex(sessionClass, SESSION_ID_PROPERTY, UNIQUE_HASH_INDEX);
      createIndex(sessionClass, LAST_ACCESSED_TIME_PROPERTY, NOTUNIQUE);
      createIndex(sessionClass, MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY, NOTUNIQUE);
    }
  }

  @PreDestroy
  public void close() {
    pool.close();
  }

  @Override
  public OrientHttpSession createSession() {
    final OrientHttpSession session = new OrientHttpSession(sessionTimeout);

    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      return db.detachAll(db.save(session), true);
    }
  }

  @Override
  public OrientHttpSession getSession(final String id) {
    final OrientHttpSession session;

    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      final List<OrientHttpSession> sessions = db.query(FIND_BY_ID_QUERY, id);
      session = sessions.isEmpty() ? null : db.detachAll(sessions.get(0), true);
    }

    if (session == null) {
      return null;
    }

    if (session.isExpired()) {
      delete(session.getId());
      return null;
    }

    session.setLastAccessedTime(System.currentTimeMillis());
    session.deserializeAttributes();

    return session;
  }

  @Override
  public void save(final OrientHttpSession session) {
    session.serializeAttributes();
    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      db.save(session);
    }
  }

  @Override
  public void delete(final String id) {
    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      db.command(DELETE_BY_ID_QUERY).execute(id);
    }
  }

  /**
   * Flushes expired sessions every minute
   */
  @Scheduled(fixedRate = 60000)
  public void flushExpiredSessions() {
    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      db.command(DELETE_EXPIRED_SESSIONS_QUERY).execute();
    }
  }

  private static void createIndex(final OClass clazz, final String property,
                                  final OClass.INDEX_TYPE indexType) {
    if (clazz.getInvolvedIndexes(property).isEmpty()) {
      clazz.getProperty(property).createIndex(indexType);
    }
  }
}
