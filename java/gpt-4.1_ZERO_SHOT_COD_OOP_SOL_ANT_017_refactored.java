import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthChecker {

    private static final Logger LOG = LoggerFactory.getLogger(HealthChecker.class);

    private final Object entity;
    private final AtomicLong stateLastGood = new AtomicLong(0);
    private final AtomicLong stateLastFail = new AtomicLong(0);

    private final AtomicReference<Long> currentFailureStartTime = new AtomicReference<>(null);
    private final AtomicReference<Long> currentRecoveryStartTime = new AtomicReference<>(null);

    private volatile LastPublished lastPublished = LastPublished.UNKNOWN;

    public HealthChecker(Object entity) {
        this.entity = entity;
    }

    // Enum for last published state
    private enum LastPublished {
        HEALTHY, FAILED, UNKNOWN
    }

    // Main entry point
    public void checkHealth() {
        CalculatedStatus status = calculateStatus();
        boolean healthy = status.isHealthy();
        long now = System.currentTimeMillis();

        if (healthy) {
            handleHealthy(now, status);
        } else {
            handleUnhealthy(now, status);
        }
    }

    // Handles healthy state transitions
    private void handleHealthy(long now, CalculatedStatus status) {
        stateLastGood.set(now);

        if (lastPublished == LastPublished.FAILED) {
            startRecovery(now, status);
        } else {
            finishFailureIfNeeded(status);
        }
    }

    // Handles unhealthy state transitions
    private void handleUnhealthy(long now, CalculatedStatus status) {
        stateLastFail.set(now);

        if (lastPublished != LastPublished.FAILED) {
            startFailure(now, status);
        } else {
            finishRecoveryIfNeeded(status);
        }
    }

    private void startRecovery(long now, CalculatedStatus status) {
        if (currentRecoveryStartTime.get() == null) {
            LOG.info("{} check for {}, now recovering: {}", this, entity, getDescription(status));
            currentRecoveryStartTime.set(now);
            schedulePublish();
        } else {
            logTrace("continuing recovering", status);
        }
    }

    private void finishFailureIfNeeded(CalculatedStatus status) {
        if (currentFailureStartTime.get() != null) {
            LOG.info("{} check for {}, now healthy: {}", this, entity, getDescription(status));
            currentFailureStartTime.set(null);
        } else {
            logTrace("still healthy", status);
        }
    }

    private void startFailure(long now, CalculatedStatus status) {
        if (currentFailureStartTime.get() == null) {
            LOG.info("{} check for {}, now failing: {}", this, entity, getDescription(status));
            currentFailureStartTime.set(now);
            schedulePublish();
        } else {
            logTrace("continuing failing", status);
        }
    }

    private void finishRecoveryIfNeeded(CalculatedStatus status) {
        if (currentRecoveryStartTime.get() != null) {
            LOG.info("{} check for {}, now failing: {}", this, entity, getDescription(status));
            currentRecoveryStartTime.set(null);
        } else {
            logTrace("still failed", status);
        }
    }

    private void logTrace(String message, CalculatedStatus status) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("{} check for {}, {}: {}", this, entity, message, getDescription(status));
        }
    }

    // Placeholder for actual status calculation
    private CalculatedStatus calculateStatus() {
        // Implement actual status calculation logic here
        return new CalculatedStatus(true); // Example
    }

    // Placeholder for description
    private String getDescription(CalculatedStatus status) {
        // Implement actual description logic here
        return status.toString();
    }

    // Placeholder for scheduling publish
    private void schedulePublish() {
        // Implement actual scheduling logic here
    }

    // Example CalculatedStatus class
    private static class CalculatedStatus {
        private final boolean healthy;

        public CalculatedStatus(boolean healthy) {
            this.healthy = healthy;
        }

        public boolean isHealthy() {
            return healthy;
        }

        @Override
        public String toString() {
            return healthy ? "HEALTHY" : "UNHEALTHY";
        }
    }
}