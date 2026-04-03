package com.ubik.motelmanagement.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class TxConfig {
    @Bean
    DatabaseClient databaseClient(ConnectionFactory cf) {
        return DatabaseClient.create(cf);
    }

    @Bean
    ReactiveTransactionManager reactiveTransactionManager(ConnectionFactory cf) {
        return new R2dbcTransactionManager(cf);
    }

    @Bean
    TransactionalOperator transactionalOperator(ReactiveTransactionManager tm) {
        return TransactionalOperator.create(tm);
    }
}
