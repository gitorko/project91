package com.demo.project91.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.demo.project91.pojo.Customer;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableIgniteRepositories("com.demo.project91.repository")
public class IgniteConfig {

    @Bean(name = "igniteInstance")
    public Ignite igniteInstance() {
        return Ignition.start(igniteConfiguration());
    }

    @Bean(name = "igniteConfiguration")
    public IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName("testIgniteInstance");
        //cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(true);
        cfg.setLocalHost("127.0.0.1");
        cfg.setMetricsLogFrequency(0);

        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setLocalPort(47500);
        // Changing local port range. This is an optional action.
        tcpDiscoverySpi.setLocalPortRange(9);
        //tcpDiscoverySpi.setLocalAddress("localhost");
        cfg.setDiscoverySpi(tcpDiscoverySpi);

        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setLocalAddress("localhost");
        communicationSpi.setLocalPort(48100);
        communicationSpi.setSlowClientQueueLimit(1000);
        cfg.setCommunicationSpi(communicationSpi);

        cfg.setCacheConfiguration(cacheConfiguration());

        return cfg;

    }

    @Bean(name = "cacheConfiguration")
    public CacheConfiguration[] cacheConfiguration() {
        List<CacheConfiguration> cacheConfigurations = new ArrayList<>();
        CacheConfiguration cc1 = new CacheConfiguration();
        cc1.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cc1.setCacheMode(CacheMode.REPLICATED);
        cc1.setName("customer");
        cc1.setStatisticsEnabled(true);

        // Defining and creating a new cache to be used by Ignite Spring Data repository.
        CacheConfiguration<Long, Customer> cc2 = new CacheConfiguration("CustomerCache");
        // Setting SQL schema for the cache.
        cc2.setIndexedTypes(Long.class, Customer.class);

        cacheConfigurations.add(cc1);
        cacheConfigurations.add(cc2);

        return cacheConfigurations.toArray(new CacheConfiguration[cacheConfigurations.size()]);
    }

}
