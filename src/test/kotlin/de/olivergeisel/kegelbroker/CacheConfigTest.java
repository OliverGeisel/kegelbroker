package de.olivergeisel.kegelbroker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("unit")
@Tag("configuration")
class CacheConfigTest {

	@Test
	void createsAConcurrentMapCacheManager() {
		var cacheManager = new CacheConfig().cacheManager();

		assertInstanceOf(ConcurrentMapCacheManager.class, cacheManager,
				"CacheConfig should use an in-memory concurrent cache manager");
	}

	@Test
	void createsTheMatchesCache() {
		CacheManager cacheManager = new CacheConfig().cacheManager();

		assertNotNull(cacheManager.getCache("matches"), "The matches cache should be available");
	}

	@Test
	void doesNotCreateCachesOutsideItsConfiguredList() {
		CacheManager cacheManager = new CacheConfig().cacheManager();

		assertNull(cacheManager.getCache("unknown"),
				"CacheConfig should not create caches that were not configured");
	}

	@Test
	void matchesCacheStoresAnEntry() {
		Cache cache = new CacheConfig().cacheManager().getCache("matches");
		var value = new Object();
		cache.put("match-one", value);

		assertSame(value, cache.get("match-one").get(), "The cache should return the stored value");
	}

	@Test
	void matchesCacheEvictsAnEntry() {
		Cache cache = new CacheConfig().cacheManager().getCache("matches");
		cache.put("match-one", new Object());

		cache.evict("match-one");
		assertNull(cache.get("match-one"), "Evicting a key should remove it from the cache");
	}
}
