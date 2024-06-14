package com.demo.project91.config;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javax.cache.configuration.Factory;
import javax.sql.DataSource;

import com.demo.project91.pojo.Customer;
import com.demo.project91.pojo.Employee;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.cache.store.jdbc.JdbcType;
import org.apache.ignite.cache.store.jdbc.JdbcTypeField;
import org.apache.ignite.cache.store.jdbc.dialect.BasicJdbcDialect;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.failure.NoOpFailureHandler;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableIgniteRepositories("com.demo.project91.repository")
public class IgniteConfig {

    @Autowired
    DataSource datasource;
    /**
     * Override the node name for each instance at start using properties
     */
    @Value("${ignite.nodeName:node1}")
    private String nodeName;

    @Bean(name = "igniteInstance")
    public Ignite igniteInstance() {
        Ignite ignite = Ignition.start(igniteConfiguration());

        /**
         * If data is persisted then have to explicitly set the cluster state to active.
         * If there are 3 nodes cluster then this is not required.
         */
        ignite.cluster().state(ClusterState.ACTIVE);
        return ignite;
    }

    @Bean(name = "igniteConfiguration")
    public IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        /**
         * Uniquely identify node in a cluster use consistent Id.
         */
        cfg.setConsistentId(nodeName);

        cfg.setIgniteInstanceName("my-ignite-instance");
        cfg.setPeerClassLoadingEnabled(true);
        cfg.setLocalHost("127.0.0.1");
        cfg.setMetricsLogFrequency(0);

        cfg.setCommunicationSpi(tcpCommunicationSpi());
        cfg.setDiscoverySpi(tcpDiscovery());
        cfg.setDataStorageConfiguration(dataStorageConfiguration());
        cfg.setCacheConfiguration(cacheConfiguration());

        /**
         * Not to be used in production
         * Ignores any failure. It's useful for tests and debugging.
         * Error: Blocked system-critical thread has been detected. This can lead to cluster-wide undefined behaviour
         * To avoid long GC pauses tune the jvm.
         */
        cfg.setFailureHandler(new NoOpFailureHandler());
        return cfg;
    }

    @Bean(name = "cacheConfiguration")
    public CacheConfiguration[] cacheConfiguration() {
        List<CacheConfiguration> cacheConfigurations = new ArrayList<>();

        /**
         * Ignite table to store Account data
         */
        CacheConfiguration cc1 = new CacheConfiguration();
        cc1.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cc1.setCacheMode(CacheMode.REPLICATED);
        cc1.setName("account-cache");
        cc1.setStatisticsEnabled(true);
        QueryEntity qe = new QueryEntity();
        qe.setTableName("ACCOUNTS");
        qe.setKeyFieldName("ID");
        qe.setKeyType("java.lang.Long");
        qe.setValueType("java.lang.Object");
        LinkedHashMap map = new LinkedHashMap();
        map.put("ID", "java.lang.Long");
        map.put("amount", "java.lang.Double");
        map.put("updateDate", "java.util.Date");
        qe.setFields(map);
        cc1.setQueryEntities(List.of(qe));

        /**
         * Customer cache to store Customer.class objects
         */
        CacheConfiguration<Long, Customer> cc2 = new CacheConfiguration("customer-cache");
        cc2.setIndexedTypes(Long.class, Customer.class);

        /**
         * Country cache to store key value pair
         */
        CacheConfiguration cc3 = new CacheConfiguration("country-cache");

        /**
         * Employee cache to store Employee.class objects
         */
        CacheConfiguration<Long, Employee> cc4 = new CacheConfiguration("employee-cache");
        cc4.setIndexedTypes(Long.class, Employee.class);
        cc4.setCacheStoreFactory(cacheJdbcPojoStoreFactory());
        /**
         * If value not present in cache then fetch from db and store in cache
         */
        cc4.setReadThrough(true);
        /**
         * If value present in cache then write to db.
         */
        cc4.setWriteThrough(true);
        /**
         * Will wait for sometime to update db asynchronously
         */
        cc4.setWriteBehindEnabled(true);
        /**
         * Min 2 entires in cache before written to db
         */
        cc4.setWriteBehindFlushSize(2);
        /**
         * Write to DB at interval delay of 2 seconds
         */
        cc4.setWriteBehindFlushFrequency(2000);
        cc4.setIndexedTypes(Long.class, Employee.class);

        cacheConfigurations.add(cc1);
        cacheConfigurations.add(cc2);
        cacheConfigurations.add(cc3);
        cacheConfigurations.add(cc4);
        return cacheConfigurations.toArray(new CacheConfiguration[cacheConfigurations.size()]);
    }

    private CacheJdbcPojoStoreFactory cacheJdbcPojoStoreFactory() {
        CacheJdbcPojoStoreFactory<Long, Employee> factory = new CacheJdbcPojoStoreFactory<>();
        factory.setDialect(new BasicJdbcDialect());
        factory.setDataSourceFactory(getDataSourceFactory());
        JdbcType employeeType = getEmployeeJdbcType();
        factory.setTypes(employeeType);
        return factory;
    }

    private TcpDiscoverySpi tcpDiscovery() {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setLocalPort(47500);
        // Changing local port range. This is an optional action.
        tcpDiscoverySpi.setLocalPortRange(9);
        //tcpDiscoverySpi.setLocalAddress("localhost");
        return tcpDiscoverySpi;
    }

    private TcpCommunicationSpi tcpCommunicationSpi() {
        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setLocalAddress("localhost");
        communicationSpi.setLocalPort(48100);
        communicationSpi.setSlowClientQueueLimit(1000);
        return communicationSpi;
    }

    private DataStorageConfiguration dataStorageConfiguration() {
        DataStorageConfiguration dsc = new DataStorageConfiguration();
        DataRegionConfiguration defaultRegionCfg = new DataRegionConfiguration();
        DataRegionConfiguration regionCfg = new DataRegionConfiguration();

        defaultRegionCfg.setName("default-data-region");
        defaultRegionCfg.setInitialSize(10485760);

        /**
         * The cache will be persisted
         */
        defaultRegionCfg.setPersistenceEnabled(true);

        regionCfg.setName("my-data-region");
        regionCfg.setInitialSize(104857600);

        dsc.setDefaultDataRegionConfiguration(defaultRegionCfg);
        dsc.setDataRegionConfigurations(regionCfg);

        return dsc;
    }

    private Factory<DataSource> getDataSourceFactory() {
        return () -> {
            DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
            driverManagerDataSource.setDriverClassName("org.postgresql.Driver");
            driverManagerDataSource.setUrl("jdbc:postgresql://localhost:5432/test-db");
            driverManagerDataSource.setUsername("test");
            driverManagerDataSource.setPassword("test@123");
            return driverManagerDataSource;
        };
    }

    private JdbcType getEmployeeJdbcType() {
        JdbcType employeeType = new JdbcType();
        employeeType.setCacheName("employee-cache");
        employeeType.setDatabaseTable("employee");
        employeeType.setKeyType(Long.class);
        employeeType.setKeyFields(new JdbcTypeField(Types.BIGINT, "id", Long.class, "id"));
        employeeType.setValueFields(
                new JdbcTypeField(Types.BIGINT, "id", Long.class, "id"),
                new JdbcTypeField(Types.VARCHAR, "name", String.class, "name"),
                new JdbcTypeField(Types.VARCHAR, "email", String.class, "email")
        );
        employeeType.setValueType(Employee.class);
        return employeeType;
    }

}
