package com.consort.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentContext {

    private static EnvironmentContext context = null;
    private Dotenv dotenv = null;
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentContext.class);

    private EnvironmentContext() {
        initEnvironment();
    }

    public static EnvironmentContext getInstance() {
        if (context == null) {
            context = new EnvironmentContext();
        }

        return context;
    }

    private void initEnvironment() {
        try {
            dotenv = Dotenv.configure().load();
        } catch (Exception e) {
            LOG.info("Dotenv configuration failed! Ignore if running on prod!", e);
        }
    }

    public String getenv(final String propertyName) {
        if(dotenv != null) {
            return dotenv.get(propertyName);
        } else {
            final String systemProperty = System.getenv(propertyName);
            if(StringUtils.isBlank(systemProperty)) {
                LOG.warn("Could not find system property -> {}!", systemProperty);
            }
            return systemProperty;
        }
    }
}
