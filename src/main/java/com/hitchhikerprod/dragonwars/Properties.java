package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.InputStream;

public class Properties {
    private static final Properties instance = new Properties();

    private static final String BASEPATH_PROP = "path.base";

    public static Properties getInstance() {
        return instance;
    }

    private final java.util.Properties props;

    private Properties() {
        try (final InputStream input = Class.forName("com.hitchhikerprod.dragonwars.Properties")
            .getResourceAsStream("system.properties")) {
            props = new java.util.Properties();
            props.load(input);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Malformed class name", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String basePath() {
        return this.props.get(BASEPATH_PROP).toString();
    }
}
