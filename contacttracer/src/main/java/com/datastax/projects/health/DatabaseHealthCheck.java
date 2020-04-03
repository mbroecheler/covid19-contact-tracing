package com.datastax.projects.health;

import com.codahale.metrics.health.HealthCheck;
import com.datastax.projects.db.DbSessionClientManager;

public class DatabaseHealthCheck extends HealthCheck {

    private final DbSessionClientManager sessionClientManager;

    public DatabaseHealthCheck(DbSessionClientManager sessionClientManager) {
        this.sessionClientManager = sessionClientManager;
    }

    @Override
    protected Result check() {
        if (sessionClientManager.isSessionClosed()) {
            return Result.unhealthy("Database connection has been closed");
        }

        // checking Session.isClosed() is not enough because if all connections to the DB get closed,
        // the driver will try to reconnect but isClosed() will not return true, so we need a way to say
        // "not connected to the DB right now"
        if (!sessionClientManager.isSessionConnected()) {
            return Result.unhealthy("Database connection is down. Disconnected from all nodes at the moment.");
        }

        return Result.healthy();
    }
}