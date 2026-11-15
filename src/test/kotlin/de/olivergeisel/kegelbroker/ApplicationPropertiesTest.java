package de.olivergeisel.kegelbroker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration")
@Tag("configuration")
class ApplicationPropertiesTest {

	@Test
	void exposesTheConfiguredDataPath() {
		new ApplicationContextRunner()
				.withUserConfiguration(ApplicationProperties.class)
				.withPropertyValues("app.dataPath=data/matches")
				.run(context -> {
					assertAll("configured application properties",
							() -> assertNull(context.getStartupFailure(),
									"A configured data path should allow context startup"),
							() -> assertEquals("data/matches",
									context.getBean(ApplicationProperties.class).getDataPath(),
									"The configured app.dataPath should be exposed by ApplicationProperties")
					);
				});
	}

	@Test
	void usesTheApplicationPropertiesDefaultWhenThereIsNoOverride() {
		new ApplicationContextRunner()
				.withInitializer(new ConfigDataApplicationContextInitializer())
				.withUserConfiguration(ApplicationProperties.class)
				.run(context -> {
					assertAll("default application properties",
							() -> assertNull(context.getStartupFailure(),
									"The application default should allow context startup"),
							() -> assertEquals("daten", context.getBean(ApplicationProperties.class).getDataPath(),
									"The default app.dataPath from application.properties should be exposed")
					);
				});
	}
}
