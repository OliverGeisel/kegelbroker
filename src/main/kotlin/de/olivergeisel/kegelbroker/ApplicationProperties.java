package de.olivergeisel.kegelbroker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

	@Value("${app.dataPath}")
	private String dataPath;


	public String getDataPath() {
		return dataPath;
	}
}
