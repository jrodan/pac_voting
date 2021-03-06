package com.prodyna.pac.voting.config;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

@Configuration
@EnableCaching
@AutoConfigureAfter(value = { MetricsConfiguration.class })
public class CacheConfiguration
{
    private static final String LOCALHOST = "127.0.0.1";

    private final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    private static HazelcastInstance hazelcastInstance;

    @Inject
    private Environment env;

    private CacheManager cacheManager;

    @PreDestroy
    public void destroy()
    {
        this.log.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }

    @Bean
    public CacheManager cacheManager(final HazelcastInstance hazelcastInstance)
    {
        this.log.debug("Starting HazelcastCacheManager");
        this.cacheManager = new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastInstance);
        return this.cacheManager;
    }

    @Bean
    public synchronized HazelcastInstance hazelcastInstance(final ApplicationProperties applicationProperties)
    {
        this.log.debug("Configuring Hazelcast");

        final Config config = new Config();
        config.setInstanceName("voting");

        config.getNetworkConfig().setPort(5701);
        config.getNetworkConfig().setPortAutoIncrement(true);

        // In development, remove multicast auto-configuration
        if (this.env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT))
        {
            System.setProperty("hazelcast.local.localAddress", LOCALHOST);

            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        }

        config.getMapConfigs().put("default", this.initializeDefaultMapConfig());
        config.getMapConfigs().put("com.prodyna.pac.voting.domain.*", this.initializeDomainMapConfig(applicationProperties));
        config.getMapConfigs().put("clustered-http-sessions", this.initializeClusteredSession(applicationProperties));

        hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config);

        return hazelcastInstance;
    }

    /**
     * Use by Spring Security, to get events from Hazelcast.
     */
    @Bean
    public SessionRegistry sessionRegistry()
    {
        return new SessionRegistryImpl();
    }

    /**
     * @return the unique instance.
     */
    public static HazelcastInstance getHazelcastInstance()
    {
        return hazelcastInstance;
    }

    private MapConfig initializeDefaultMapConfig()
    {
        final MapConfig mapConfig = new MapConfig();

        /*
         * Number of backups. If 1 is set as the backup-count for example, then all entries of the map will be copied to another JVM for
         * fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
         */
        mapConfig.setBackupCount(0);

        /*
         * Valid values are: NONE (no eviction), LRU (Least Recently Used), LFU (Least Frequently Used). NONE is the default.
         */
        mapConfig.setEvictionPolicy(EvictionPolicy.LRU);

        /*
         * Maximum size of the map. When max size is reached, map is evicted based on the policy defined. Any integer between 0 and
         * Integer.MAX_VALUE. 0 means Integer.MAX_VALUE. Default is 0.
         */
        mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE));

        /*
         * When max. size is reached, specified percentage of the map will be evicted. Any integer between 0 and 100. If 25 is set for
         * example, 25% of the entries will get evicted.
         */
        mapConfig.setEvictionPercentage(25);

        return mapConfig;
    }

    private MapConfig initializeDomainMapConfig(final ApplicationProperties applicationProperties)
    {
        final MapConfig mapConfig = new MapConfig();

        mapConfig.setTimeToLiveSeconds(applicationProperties.getCache().getTimeToLiveSeconds());
        return mapConfig;
    }

    private MapConfig initializeClusteredSession(final ApplicationProperties applicationProperties)
    {
        final MapConfig mapConfig = new MapConfig();

        mapConfig.setBackupCount(applicationProperties.getCache().getHazelcast().getBackupCount());
        mapConfig.setTimeToLiveSeconds(applicationProperties.getCache().getTimeToLiveSeconds());
        return mapConfig;
    }

}
