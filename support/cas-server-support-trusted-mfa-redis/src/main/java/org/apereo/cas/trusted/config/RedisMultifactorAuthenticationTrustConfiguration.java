package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.RedisObjectFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.RedisMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.cryto.CipherExecutor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link RedisMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Configuration("redisMultifactorAuthenticationTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisMultifactorAuthenticationTrustConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private ObjectProvider<CipherExecutor> mfaTrustCipherExecutor;
    
    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceMfaTrustedAuthnExecptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisMfaTrustedAuthnConnectionFactory")
    public RedisConnectionFactory redisMfaTrustedAuthnConnectionFactory() {
        val redis = casProperties.getAuthn().getMfa().getTrusted().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisMfaTrustedAuthnTemplate")
    public RedisTemplate redisMfaTrustedAuthnTemplate {
        return RedisObjectFactory.newRedisTemplate(redisMfaTrustedAuthnConnectionFactory());
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        val redis = casProperties.getAuthn().getMfa().getTrusted().getRedis();
        val r =
            new RedisMultifactorAuthenticationTrustStorage(
                    redisMfaTrustedAuthnConnectionFactory);
        r.setCipherExecutor(mfaTrustCipherExecutor.getObject());
        return r;
    }
}

