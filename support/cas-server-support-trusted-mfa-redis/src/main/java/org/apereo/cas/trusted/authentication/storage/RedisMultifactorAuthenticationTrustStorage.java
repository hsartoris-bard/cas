package org.apereo.cas.trusted.authentication.storage;

import java.time.LocalDateTime;
import java.util.Set;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link RedisMultifactorAuthenticationTrustStorage}.
 *
 * @Author Hayden Sartoris
 * @since 6.2.0
 */

@Slf4j
@RequiredArgsConstructor
public class RedisMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
	private static final String KEY_SEPARATOR = ":";
	private static final String CAS_PREFIX = RedisMultifactorAuthenticationTrustStorage.class.getSimpleName();

	private final String collectionName;
	private final RedisTemplate redisTemplate;
	/*
    @Override
    public void expire(final String key) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("recordKey").is(key));
            val res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getDeletedCount());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.info("No trusted authentication records could be found");
            }
        }
    }

    @Override
    public void expire(final LocalDateTime onOrBefore) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("recordDate").lte(onOrBefore));
            val res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getDeletedCount());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.info("No trusted authentication records could be found");
            }
        }
    }
	*/

    //@Override
    //public Set<? extends MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
    //    val query = new Query();
    //    query.addCriteria(Criteria.where("recordDate").gte(onOrAfterDate));
    //    val results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
    //    return new HashSet<>(results);
    //}

    //@Override
    //public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
    //    val query = new Query();
    //    query.addCriteria(Criteria.where("principal").is(principal));
    //    val results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
    //    return new HashSet<>(results);
    //}
	@Override
	public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
		return null;
	}
	@Override
	public MultifactorAuthenticationTrustRecord get(final long id) {
		// TODO
        //val query = new Query();
        //query.addCriteria(Criteria.where("id").is(id));
        //return mongoTemplate.findOne(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
		return null;
	}

	@Override
	protected MultifactorAuthenticationTrustRecord setInternal(MultifactorAuthenticationTrustRecord record) {
		// TODO Auto-generated method stub
        //this.mongoTemplate.save(record, this.collectionName);
        //return record;
		return null;
	}
	private static String getMfaAuthnRedisKey(final String principal) {
		return CAS_PREFIX + KEY_SEPARATOR + principal;
	}
}

