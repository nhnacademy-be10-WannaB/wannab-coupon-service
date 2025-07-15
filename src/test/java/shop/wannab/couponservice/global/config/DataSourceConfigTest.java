package shop.wannab.couponservice.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        properties = {
                "dbcp2.datasource.driverClassName=org.h2.Driver",
                "dbcp2.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
                "dbcp2.datasource.username=sa",
                "dbcp2.datasource.password=",
                "dbcp2.datasource.maxIdle=10",
                "dbcp2.datasource.maxTotal=20",
                "dbcp2.datasource.initialSize=5",
                "dbcp2.datasource.minIdle=5",
                "dbcp2.datasource.betweenEvictionMillis=60000",
                "dbcp2.datasource.minEvictableIdleMillis=300000",
                "dbcp2.datasource.numTestsPerEvictionRun=3"
        },

        classes = {DataSourceConfig.class}
)
@EnableConfigurationProperties(DataSourceConfig.class)
@TestPropertySource(properties = {
        "dbcp2.datasource.driverClassName=org.h2.Driver",
        "dbcp2.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
        "dbcp2.datasource.username=sa",
        "dbcp2.datasource.password=",
        "dbcp2.datasource.maxIdle=10",
        "dbcp2.datasource.maxTotal=20",
        "dbcp2.datasource.initialSize=5",
        "dbcp2.datasource.minIdle=5",
        "dbcp2.datasource.betweenEvictionMillis=60000",
        "dbcp2.datasource.minEvictableIdleMillis=300000",
        "dbcp2.datasource.numTestsPerEvictionRun=3"
})
@DisplayName("DataSource 설정 테스트")
class DataSourceConfigTest {

    @Autowired
    private DataSourceConfig dataSourceConfig;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("ConfigurationProperties가 올바르게 바인딩되는지 확인")
    void configurationPropertiesAreBoundCorrectly() {
        assertThat(dataSourceConfig).isNotNull();
        assertThat(dataSourceConfig.getDriverClassName()).isEqualTo("org.h2.Driver");
        assertThat(dataSourceConfig.getUrl()).isEqualTo("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL");
        assertThat(dataSourceConfig.getUsername()).isEqualTo("sa");
        assertThat(dataSourceConfig.getPassword()).isEqualTo("");
        assertThat(dataSourceConfig.getMaxIdle()).isEqualTo(10);
        assertThat(dataSourceConfig.getMaxTotal()).isEqualTo(20);
        assertThat(dataSourceConfig.getInitialSize()).isEqualTo(5);
        assertThat(dataSourceConfig.getMinIdle()).isEqualTo(5);
        assertThat(dataSourceConfig.getBetweenEvictionMillis()).isEqualTo(60000L);
        assertThat(dataSourceConfig.getMinEvictableIdleMillis()).isEqualTo(300000L);
        assertThat(dataSourceConfig.getNumTestsPerEvictionRun()).isEqualTo(3);
    }

    @Test
    @DisplayName("DataSource 빈이 올바르게 생성되고 설정되는지 확인")
    void dataSourceBeanIsConfiguredCorrectly() {
        assertThat(dataSource).isNotNull();
        assertThat(dataSource).isInstanceOf(BasicDataSource.class);
        BasicDataSource basicDataSource = (BasicDataSource) dataSource;

        assertThat(basicDataSource.getDriverClassName()).isEqualTo("org.h2.Driver");
        assertThat(basicDataSource.getUrl()).isEqualTo("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL");
        assertThat(basicDataSource.getUsername()).isEqualTo("sa");
        assertThat(basicDataSource.getPassword()).isEqualTo("");
        assertThat(basicDataSource.getMaxIdle()).isEqualTo(10);
        assertThat(basicDataSource.getMaxTotal()).isEqualTo(20);
        assertThat(basicDataSource.getInitialSize()).isEqualTo(5);
        assertThat(basicDataSource.getMinIdle()).isEqualTo(5);
        assertThat(dataSourceConfig.getBetweenEvictionMillis()).isEqualTo(60000L);
        assertThat(dataSourceConfig.getMinEvictableIdleMillis()).isEqualTo(300000L);
        assertThat(dataSourceConfig.getNumTestsPerEvictionRun()).isEqualTo(3);

        assertThat(basicDataSource.getValidationQuery()).isEqualTo("SELECT 1");
        assertThat(basicDataSource.getTestOnReturn()).isTrue();
        assertThat(basicDataSource.getTestOnBorrow()).isTrue();
        assertThat(basicDataSource.getTestWhileIdle()).isTrue();

        assertThat(basicDataSource.getDurationBetweenEvictionRuns().toMillis()).isEqualTo(60000L * 60 * 1000);
        assertThat(basicDataSource.getMinEvictableIdleDuration().toMillis()).isEqualTo(300000L * 60 * 1000);
        assertThat(basicDataSource.getNumTestsPerEvictionRun()).isEqualTo(3);
    }
}