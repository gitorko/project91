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
import org.apache.ignite.configuration.DataPageEvictionMode;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableIgniteRepositories("com.demo.project91.repository")
public class IgniteConfig {

    /**
     * Override the node name for each instance at start using properties
     */
    @Value("${ignite.nodeName:node0}")
    private String nodeName;

    @Value("${ignite.kubernetes.enabled:false}")
    private Boolean k8sEnabled;

    @Bean(name = "igniteInstance")
    public Ignite igniteInstance() {
        Ignite ignite = Ignition.start(igniteConfiguration());

        /**
         * If data is persisted then have to explicitly set the cluster state to active.
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
        if (k8sEnabled) {
            cfg.setDiscoverySpi(tcpDiscoverySpiKubernetes());
        } else {
            cfg.setDiscoverySpi(tcpDiscovery());
        }
        cfg.setDataStorageConfiguration(dataStorageConfiguration());
        cfg.setCacheConfiguration(cacheConfiguration());
        return cfg;
    }

    @Bean(name = "cacheConfiguration")
    public CacheConfiguration[] cacheConfiguration() {
        List<CacheConfiguration> cacheConfigurations = new ArrayList<>();
        cacheConfigurations.add(getAccountCacheConfig());
        cacheConfigurations.add(getCustomerCacheConfig());
        cacheConfigurations.add(getCountryCacheConfig());
        cacheConfigurations.add(getEmployeeCacheConfig());
        return cacheConfigurations.toArray(new CacheConfiguration[cacheConfigurations.size()]);
    }

    private CacheConfiguration getAccountCacheConfig() {
        /**
         * Ignite table to store Account data
         */
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cacheConfig.setCacheMode(CacheMode.REPLICATED);
        cacheConfig.setName("account-cache");
        cacheConfig.setStatisticsEnabled(true);
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
        cacheConfig.setQueryEntities(List.of(qe));
        return cacheConfig;
    }

    private CacheConfiguration<Long, Customer> getCustomerCacheConfig() {
        /**
         * Customer cache to store Customer.class objects
         */
        CacheConfiguration<Long, Customer> cacheConfig = new CacheConfiguration("customer-cache");
        cacheConfig.setIndexedTypes(Long.class, Customer.class);
        return cacheConfig;
    }

    private CacheConfiguration getCountryCacheConfig() {
        /**
         * Country cache to store key value pair
         */
        CacheConfiguration cacheConfig = new CacheConfiguration("country-cache");
        /**
         * This cache will be stored in non-persistent data region
         */
        cacheConfig.setDataRegionName("my-data-region");
        return cacheConfig;
    }

    private CacheConfiguration<Long, Employee> getEmployeeCacheConfig() {
        /**
         * Employee cache to store Employee.class objects
         */
        CacheConfiguration<Long, Employee> cacheConfig = new CacheConfiguration("employee-cache");
        cacheConfig.setIndexedTypes(Long.class, Employee.class);
        cacheConfig.setCacheStoreFactory(cacheJdbcPojoStoreFactory());
        /**
         * If value not present in cache then fetch from db and store in cache
         */
        cacheConfig.setReadThrough(true);
        /**
         * If value present in cache then write to db.
         */
        cacheConfig.setWriteThrough(true);
        /**
         * Will wait for sometime to update db asynchronously
         */
        cacheConfig.setWriteBehindEnabled(true);
        /**
         * Min 2 entires in cache before written to db
         */
        cacheConfig.setWriteBehindFlushSize(2);
        /**
         * Write to DB at interval delay of 2 seconds
         */
        cacheConfig.setWriteBehindFlushFrequency(2000);
        cacheConfig.setIndexedTypes(Long.class, Employee.class);
        return cacheConfig;
    }

    private CacheJdbcPojoStoreFactory cacheJdbcPojoStoreFactory() {
        CacheJdbcPojoStoreFactory<Long, Employee> factory = new CacheJdbcPojoStoreFactory<>();
        factory.setDialect(new BasicJdbcDialect());

        //factory.setDataSourceFactory(getDataSourceFactory());
        factory.setDataSourceFactory(new DbFactory());
        JdbcType employeeType = getEmployeeJdbcType();
        factory.setTypes(employeeType);
        return factory;
    }

    /**
     * Nodes discover each other over this port
     */
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

    private TcpDiscoverySpi tcpDiscoverySpiKubernetes() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryKubernetesIpFinder ipFinder = new TcpDiscoveryKubernetesIpFinder();
        spi.setIpFinder(ipFinder);
        return spi;
    }

    /**
     * Nodes communicate with each other over this port
     */
    private TcpCommunicationSpi tcpCommunicationSpi() {
        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setMessageQueueLimit(1024);
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
         * The cache will be persisted on default region
         */
        defaultRegionCfg.setPersistenceEnabled(true);

        /**
         * Eviction mode
         */
        defaultRegionCfg.setPageEvictionMode(DataPageEvictionMode.RANDOM_LRU);

        regionCfg.setName("my-data-region");
        regionCfg.setInitialSize(104857600);
        /**
         * Cache in this region will not be persisted
         */
        regionCfg.setPersistenceEnabled(false);

        dsc.setDefaultDataRegionConfiguration(defaultRegionCfg);
        dsc.setDataRegionConfigurations(regionCfg);

        return dsc;
    }

    /**
     * Since it serializes you cant pass variables. Use the DbFactory.class
     */
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
