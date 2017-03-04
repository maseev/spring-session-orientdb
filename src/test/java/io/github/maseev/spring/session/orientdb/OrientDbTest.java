package io.github.maseev.spring.session.orientdb;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.maseev.spring.session.orientdb.configuration.TestConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public abstract class OrientDbTest {

  @Autowired
  protected SessionRepository<OrientHttpSession> repository;

  @Autowired
  protected OPartitionedDatabasePool pool;

  @After
  public void after() {
    final String DELETE_ALL_SESSIONS_QUERY =
      "DELETE FROM " + OrientHttpSession.class.getSimpleName();

    try (final OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire())) {
      db.command(new OCommandSQL(DELETE_ALL_SESSIONS_QUERY)).execute();
    }
  }
}
