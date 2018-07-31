package io.github.maseev.spring.session.orientdb;

interface SessionRemover {

  void flushExpiredSessions();
}
