package com.devops.springmongo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database:devops_db}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
}