package de.olivergeisel.kegelbroker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
@EnableScheduling
class KegelbrokerApplication

fun main(args: Array<String>) {
	runApplication<KegelbrokerApplication>(*args)
}

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig : WebMvcConfigurer {

	override fun addViewControllers(registry: ViewControllerRegistry) {
		registry.addViewController("/login").setViewName("login")
	}

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

		http.csrf { it.disable() }
		http.authorizeHttpRequests { it.anyRequest().permitAll() }
		http.formLogin { }
		http.cors { }
		http.logout { it.logoutSuccessUrl("/").clearAuthentication(true).deleteCookies("JSESSIONID") }
		return http.build();
	}
}
