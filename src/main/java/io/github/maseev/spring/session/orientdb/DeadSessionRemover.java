package io.github.maseev.spring.session.orientdb;

import org.springframework.scheduling.annotation.Scheduled;

class DeadSessionRemover implements SessionRemover {

  private final OrientHttpSessionRepository repository;

  DeadSessionRemover(OrientHttpSessionRepository repository) {
    this.repository = repository;
  }

  @Override
  @Scheduled(fixedRate = 60000)
  public void flushExpiredSessions() {
    repository.deleteExpiredSessions();
  }
}
