package tech.ydb.table.settings;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * @author Sergey Polovko
 */
public class RequestSettings<Self extends RequestSettings> {

    private String traceId;
    private long deadlineAfter;
    private Duration operationTimeout;
    private Duration cancelAfter;
    private Duration timeout;

    public String getTraceId() {
        return traceId;
    }

    public Self setTraceId(String traceId) {
        this.traceId = traceId;
        return self();
    }

    public Optional<Duration> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    /**
     * Sets a client timeout. After this much time since request is sent there is no reason to process it.
     * Please consider also setting a server timeout (OperationTimeout or CancelAfter) with a value lower
     * than [client] Timeout.
     * Timeout has higher priority than DeadlineAfter.
     *
     * @param duration  Duration
     * @return this
     */
    public Self setTimeout(Duration duration) {
        if (duration.compareTo(Duration.ZERO) >= 0) {
            this.timeout = duration;
        }
        return self();
    }

    public Self setTimeout(long duration, TimeUnit unit) {
        if (duration >= 0) {
            this.timeout = Duration.ofNanos(unit.toNanos(duration));
        }
        return self();
    }

    public long getDeadlineAfter() {
        return deadlineAfter;
    }

    /**
     * This method is deprecated. Please consider using setTimeout instead.
     * Sets an instantaneous point on the time-line after which there is no reason to process request.
     * Setting Timeout has higher priority than setting DeadlineAfter
     *
     * @param deadlineAfter  the number of nanoseconds from the UNIX-epoch
     * @return this
     */
    public Self setDeadlineAfter(long deadlineAfter) {
        this.deadlineAfter = Math.max(0, deadlineAfter);
        return self();
    }

    @SuppressWarnings("unchecked")
    private Self self() {
        return (Self) this;
    }

    public Optional<Duration> getOperationTimeout() {
        return Optional.ofNullable(operationTimeout);
    }

    public Self setOperationTimeout(Duration operationTimeout) {
        this.operationTimeout = operationTimeout;
        return self();
    }

    public Optional<Duration> getCancelAfter() {
        return Optional.ofNullable(cancelAfter);
    }

    public Self setCancelAfter(Duration cancelAfter) {
        this.cancelAfter = cancelAfter;
        return self();
    }
}
