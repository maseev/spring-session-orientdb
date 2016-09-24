package io.github.maseev.spring.session.orientdb;

import static io.github.maseev.spring.session.orientdb.OrientHttpSession.LAST_ACCESSED_TIME_PROPERTY;
import static io.github.maseev.spring.session.orientdb.OrientHttpSession.MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY;
import static io.github.maseev.spring.session.orientdb.OrientHttpSession.SESSION_ID_PROPERTY;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import javax.annotation.PreDestroy;

@Repository
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

  private final OObjectDatabaseTx db;

  private final int sessionTimeout;

  public OrientHttpSessionRepository(final OObjectDatabaseTx db, final int sessionTimeout) {
    this.db = db;
    this.sessionTimeout = sessionTimeout;

    db.setAutomaticSchemaGeneration(true);
    db.getEntityManager().registerEntityClass(OrientHttpSession.class);
    db.getMetadata().getSchema().synchronizeSchema();

    final OClass sessionClass = db.getMetadata().getSchema().getClass(OrientHttpSession.class);
    sessionClass.getProperty(SESSION_ID_PROPERTY)
      .createIndex(OClass.INDEX_TYPE.UNIQUE_HASH_INDEX);
    sessionClass.getProperty(LAST_ACCESSED_TIME_PROPERTY)
      .createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
    sessionClass.getProperty(MAX_INACTIVE_INTERVAL_IN_SECONDS_PROPERTY)
      .createIndex(OClass.INDEX_TYPE.NOTUNIQUE);
  }

  @PreDestroy
  public void close() {
    db.close();
  }

  @Override
  public OrientHttpSession createSession() {
    final OrientHttpSession session = new OrientHttpSession(sessionTimeout);
    return db.detachAll(db.save(session), true);
  }

  @Override
  public OrientHttpSession getSession(final String id) {
    final List<OrientHttpSession> sessions = db.query(FIND_BY_ID_QUERY, id);
    final OrientHttpSession session =
      sessions.isEmpty() ? null : db.detachAll(sessions.get(0), true);

    if (session == null) {
      return null;
    }

    if (session.isExpired()) {
      delete(session.getId());
      return null;
    }

    session.setLastAccessedTime(System.currentTimeMillis());

    return session;
  }

  @Override
  public void save(OrientHttpSession session) {
    db.save(session);
  }

  @Override
  public void delete(String id) {
    db.command(DELETE_BY_ID_QUERY).execute(id);
  }

  /**
   * Flushes expired sessions every minute
   * */
  @Scheduled(fixedRate = 60000)
  public void flushExpiredSessions() {
    db.command(DELETE_EXPIRED_SESSIONS_QUERY).execute();
  }
}
