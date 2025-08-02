package com.zengcode.config.camelrouter;

import com.zengcode.config.service.external.AExternalImporter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import zengcode.config.common.utillity.DistributedLock;

@Component
@RequiredArgsConstructor
public class S3PollingRoute extends RouteBuilder {

    private static final String S_3_POLLING_ROUTE = "s3-polling-route";
    private final DistributedLock distributedLock;
    private final AExternalImporter s3ExternalImporter;


    private static final String ERROR_PREFIX = "error/";

    @Override
    public void configure() {
        from("timer:s3-poll?period={{config.polling.period-ms}}")
                .routeId(S_3_POLLING_ROUTE)
                .process(exchange -> {
                    distributedLock.withDistributedLock("s3-pooling-lock", 5, 120, () -> {
                        s3ExternalImporter.importToConfiguration();
                        return null;
                    });
                });
    }

}
