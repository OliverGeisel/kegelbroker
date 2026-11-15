package de.olivergeisel.kegelbroker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@Tag("configuration")
class CacheConfigIntegrationTest {

	@Test
	void cacheableMethodReturnsItsResult() {
		try (var context = new AnnotationConfigApplicationContext(TestConfiguration.class)) {
			var operation = context.getBean(CachedOperation.class);

			assertEquals("match-1", operation.load("match-1"),
					"The first call should return the operation result");
		}
	}

	@Test
	void repeatedCacheableCallsUseTheMatchesCache() {
		try (var context = new AnnotationConfigApplicationContext(TestConfiguration.class)) {
			var operation = context.getBean(CachedOperation.class);

			assertAll("repeated cached operation",
					() -> assertEquals("match-1", operation.load("match-1"),
							"The first call should return the operation result"),
					() -> assertEquals("match-1", operation.load("match-1"),
							"The repeated call should return the cached operation result"),
					() -> assertEquals(1, operation.getInvocationCount(),
							"An identical second call should be served from the matches cache")
			);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@Import(CacheConfig.class)
	static class TestConfiguration {

		@Bean
		CachedOperation cachedOperation() {
			return new CachedOperation();
		}
	}

	static class CachedOperation {

		private int invocationCount;

		@Cacheable("matches")
		public String load(String matchName) {
			invocationCount++;
			return matchName;
		}

		int getInvocationCount() {
			return invocationCount;
		}
	}
}
