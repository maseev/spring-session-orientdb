package io.github.maseev.spring.session.orientdb;

class NoOpSessionRemover implements SessionRemover {

  @Override
  public void flushExpiredSessions() {
    // do nothing
  }
}
