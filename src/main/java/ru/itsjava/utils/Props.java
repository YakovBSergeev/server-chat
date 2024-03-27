package ru.itsjava.utils;

import lombok.SneakyThrows;

import java.util.Properties;

public class Props {
    private final Properties properties;

    @SneakyThrows
    public Props() {
        this.properties = new Properties();
        properties.load( Props.class.getClassLoader().getResourceAsStream( "application.properties" ) );
    }

    public String getValue(String key){
        return properties.getProperty(key);
    }

}

