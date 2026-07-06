package com.twp.util;

import java.io.InputStream;
import java.util.Properties;

public class EnvConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = EnvConfig.class.getResourceAsStream("/config.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("Aviso: Arquivo /config.properties não encontrado.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
