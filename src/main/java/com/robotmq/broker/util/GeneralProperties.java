package com.robotmq.broker.util;

import javax.el.PropertyNotFoundException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class GeneralProperties {

    private Logger log = Logger.getLogger(GeneralProperties.class.getName());

    private final static GeneralProperties INSTANCE = new GeneralProperties();
    private InputStream inputStream;

    private GeneralProperties() {
    }

    public static GeneralProperties getINSTANCE() {
        return INSTANCE;
    }

    public String getPropertyValue(String propFileName, String propertyName) {
        String result = null;
        try {
            Properties properties = new Properties();
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            result = properties.getProperty(propertyName);
            if (result == null) {
                throw new PropertyNotFoundException();
            }
        } catch (Exception e) {
            log.severe(e.toString());
        }
        return result;
    }


}
