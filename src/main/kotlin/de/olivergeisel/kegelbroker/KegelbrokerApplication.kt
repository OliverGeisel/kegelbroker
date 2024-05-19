package de.olivergeisel.kegelbroker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
@EnableScheduling
class KegelbrokerApplication

fun main(args: Array<String>) {
	runApplication<KegelbrokerApplication>(*args)
}
