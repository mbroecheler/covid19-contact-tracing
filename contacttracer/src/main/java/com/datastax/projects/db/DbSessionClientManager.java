package com.datastax.projects.db;


import com.datastax.dse.driver.api.core.graph.*;
import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.connection.ConnectionInitException;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.dropwizard.lifecycle.Managed;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 * Handles application lifecycle operations regarding DseSession management.
 */
public class DbSessionClientManager implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSessionClientManager.class);

    private final CqlSessionBuilder sessionBuilder;
    private final String keyspace;
    private final boolean createSchema;


    private final CompletableFuture<CqlSession> sessionFuture = new CompletableFuture<>();

    CqlSessionBuilder sessionBuilder() {
        return sessionBuilder;
    }

    public DbSessionClientManager(DatabaseConfiguration dbConfig) {
        this.sessionBuilder = CqlSession.builder()
                .withLocalDatacenter("Graph")
                .addContactPoint(dbConfig.getSeeds().stream()
                        .map(s -> new InetSocketAddress(s.getHost(), s.getPort())).findFirst().get())
                .withApplicationName("COVID19 Contact Tracing");
        this.keyspace = dbConfig.getKeyspace();
        this.createSchema = dbConfig.isCreateSchema();
    }

    @Override
    public void start() {
        LOGGER.info("Starting database connection.");
        tryReconnect()
                .thenAccept(dseSession -> {
                    try {
                        //TODO: initialize database if not already done so
                        if (createSchema) {
                            LOGGER.info("Creating schema.");
                            DbSchema.createSchema(keyspace,dseSession);
                        }

                        sessionFuture.complete(dseSession);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not initialize the database.", e);
                    }
                })
                .exceptionally(e -> {
                    LOGGER.error("Error when trying to connect to the database.", e);
                    sessionFuture.completeExceptionally(e);
                    return null;
                });
    }

    private CompletionStage<CqlSession> newSessionConnection() {
        return sessionBuilder().buildAsync();
    }

    private CompletionStage<CqlSession> tryReconnect() {
        return newSessionConnection()
                // handleAsync with another executor so the following processing doesn't happen on a driver thread.
                .handleAsync((session, ex) -> {
                    if (ex != null) {
                        if (shouldRetry(ex)) {
                            int delaySec = 2;
                            LOGGER.warn("Initial database connection failed, retrying connection in {} seconds", delaySec);
                            // retry connection
                            // TODO: add a retry limit
                            // TODO: see if 2 seconds is good delay
                            return scheduleExecIn(2, TimeUnit.SECONDS)
                                    .thenCompose(f -> tryReconnect());
                        } else {
                            return failed(ex, CqlSession.class);
                        }
                    } else {
                        LOGGER.info("Successful connection to the database.");
                        return CompletableFuture.completedFuture(session);
                    }
                }, BACKOFF_RETRY_SCHEDULER)
                .thenCompose(Function.identity());
    }

    private static final ScheduledExecutorService BACKOFF_RETRY_SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    /**
     * Schedules a {@link CompletionStage} that will complete in the given delay.
     */
    private static CompletionStage<Void> scheduleExecIn(long delay, TimeUnit unit) {
        CompletableFuture<Void> comp = new CompletableFuture<>();
        if (delay == 0) {
            BACKOFF_RETRY_SCHEDULER.submit(() -> comp.complete(null));
        } else {
            BACKOFF_RETRY_SCHEDULER.schedule(() -> comp.complete(null), delay, unit);
        }
        return comp;
    }

    /**
     * Creates a {@link CompletableFuture} that has already completed exceptionally with the given {@code error}.
     *
     * @param error the returned {@link CompletionStage} should immediately complete with this {@link Throwable}
     * @return a {@link CompletableFuture} that has already completed with the given {@code error}
     */
    private static <T> CompletionStage<T> failed(Throwable error, Class<T> clazz) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }

    private static boolean shouldRetry(Throwable e) {
        return (e != null && isAllConnectionInitException(e.getCause()));
    }

    public static boolean isAllConnectionInitException(Throwable ex) {
        // We don't want NoNodeAvailableException because that means no host was tried = misconfiguration
        if (ex instanceof AllNodesFailedException && !(ex instanceof NoNodeAvailableException)) {
            AllNodesFailedException anfe = ((AllNodesFailedException) ex);
            return checkAllExceptionsInANFE(anfe, ConnectionInitException.class);
        }
        return false;
    }

    private static boolean checkAllExceptionsInANFE(AllNodesFailedException ex, Class exceptionClass) {
        Collection<Throwable> exceptions = ex.getErrors().values();
        for (Throwable exception : exceptions) {
            if (!(exception.getClass().isAssignableFrom(exceptionClass))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void stop() {
        if (sessionFuture.isDone() && !sessionFuture.isCompletedExceptionally()) {
            CqlSession session = getSession();
            if (session != null) {
                LOGGER.info("Closing connection to the database");
                // Graceful stop
                session.close();
            }
        }
    }

    public CqlSession getSession() {
        try {
            if (!sessionFuture.isDone()) {
                throw new NotReadyException();
            }
            return sessionFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Database connection initialization failed.", e);
        } catch (NotReadyException e) {
            throw new RuntimeException("Database session not ready.");
        }
    }

    public boolean isSessionConnected() {
        CqlSession session = getSession();
        for (Node node : session.getMetadata().getNodes().values()) {
            if (node.getOpenConnections() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isSessionClosed() {
        return getSession().isClosed();
    }


    public GraphResultSet executeGraphTraversal(GraphTraversal traversal) {
        FluentGraphStatement stmt =  FluentGraphStatement.newInstance(traversal).setGraphName(keyspace);
        return getSession().execute(stmt);
    }


}