package com.zengcode.config.service.external;

import com.zengcode.config.service.IConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import zengcode.config.common.dto.ConfigRequest;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public abstract class AExternalImporter {

    private final IConfiguration configurationService;

    abstract ConfigRequest readFromSource();

    public void importToConfiguration() {
        try {
            configurationService.upsertConfigurations(readFromSource());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }
}

