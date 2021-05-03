package com.viettel.vtag.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @Primary
    @Bean("primary-db")
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource1() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean("primary-jdbc")
    public JdbcTemplate jdbcTemplate1(@Qualifier("primary-db") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean("sms-db")
    @ConfigurationProperties("spring.sms-datasource")
    public DataSource dataSource2() {
        return DataSourceBuilder.create().build();
    }

    @Bean("sms-jdbc")
    public JdbcTemplate jdbcTemplate2(@Qualifier("sms-db") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
