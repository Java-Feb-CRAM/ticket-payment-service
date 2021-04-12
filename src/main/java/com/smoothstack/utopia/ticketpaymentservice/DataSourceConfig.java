package com.smoothstack.utopia.ticketpaymentservice;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Rob Maes
 * Apr 12 2021
 */
@Configuration
@Profile(value = { "dev", "prod" })
public class DataSourceConfig {

  @Value("#{${UT_MYSQL}['host']}")
  private String mysqlHost;

  @Value("#{${UT_MYSQL}['port']}")
  private String mysqlPort;

  @Value("#{${UT_MYSQL}['password']}")
  private String mysqlPass;

  @Value("#{${UT_MYSQL}['username']}")
  private String mysqlUser;

  @Bean
  public DataSource getDataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.url(
      "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/utopia"
    );
    dataSourceBuilder.username(mysqlUser);
    dataSourceBuilder.password(mysqlPass);
    return dataSourceBuilder.build();
  }
}
