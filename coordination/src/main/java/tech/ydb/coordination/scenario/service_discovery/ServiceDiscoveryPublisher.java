package tech.ydb.coordination.scenario.service_discovery;

import java.nio.charset.StandardCharsets;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.ydb.coordination.CoordinationClient;
import tech.ydb.coordination.SessionRequest;
import tech.ydb.coordination.observer.CoordinationSessionObserver;
import tech.ydb.coordination.scenario.WorkingScenario;
import tech.ydb.coordination.session.CoordinationSession;
import tech.ydb.coordination.settings.ScenarioSettings;
import tech.ydb.core.Status;

/**
 * @author Kirill Kurdyukov
 */
public class ServiceDiscoveryPublisher extends WorkingScenario {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryPublisher.class);

    private ServiceDiscoveryPublisher(CoordinationClient client, ScenarioSettings settings) {
        super(client, settings);
    }

    public static Builder newBuilder(CoordinationClient client, String endpoint) {
        return new Builder(client, endpoint);
    }

    public static class Builder extends ScenarioSettings.Builder<ServiceDiscoveryPublisher> {

        private final String endpoint;

        public Builder(CoordinationClient client, String endpoint) {
            super(client);

            this.endpoint = endpoint;
        }

        @Override
        protected ServiceDiscoveryPublisher buildScenario(ScenarioSettings settings) {
            ServiceDiscoveryPublisher publisher = new ServiceDiscoveryPublisher(client, settings);

            publisher.start(
                    new CoordinationSessionObserver() {
                        @Override
                        public void onSessionStarted() {
                            CoordinationSession coordinationSession = publisher.currentCoordinationSession.get();

                            logger.info("Starting service discovery publisher session, sessionId: {}",
                                    coordinationSession.getSessionId());

                            coordinationSession.sendCreateSemaphore(
                                    SessionRequest.CreateSemaphore.newBuilder()
                                            .setName(settings.getSemaphoreName())
                                            .setLimit(Long.MAX_VALUE)
                                            .build()
                            );

                            coordinationSession.sendAcquireSemaphore(
                                    SessionRequest.AcquireSemaphore.newBuilder()
                                            .setName(settings.getSemaphoreName())
                                            .setCount(1)
                                            .setData(ByteString.copyFrom(endpoint.getBytes(StandardCharsets.UTF_8)))
                                            .build()
                            );
                        }

                        @Override
                        public void onAcquireSemaphoreResult(boolean acquired, Status status) {
                            logger.info("Endpoint publish: {}!", endpoint);

                            assert acquired && status.isSuccess();
                        }

                        @Override
                        public void onFailure(Status status) {
                            logger.error("Fail from publisher session: {}", status);
                        }
                    }
            );

            return publisher;
        }
    }
}
