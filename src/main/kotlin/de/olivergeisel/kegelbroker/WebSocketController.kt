/*
 * Copyright 2023 Oliver Geisel
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.olivergeisel.kegelbroker

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.util.HtmlUtils

@Controller
class WebSocketController {

	@GetMapping("/ws")
	fun ws(): String {
		return "ws"
	}

	@MessageMapping("/hello")
	@SendTo("/ws-response/greetings")
	@ResponseBody
	fun greeting(name: Greeting): Message {
		Thread.sleep(1000) // simulated delay
		val t = Message("Hello, ${HtmlUtils.htmlEscape(name.name)}!")
		return t
	}
}

data class Message(val name: String) {
	override fun toString(): String {
		return "Message(name='$name')"
	}
}

class Greeting(val name: String) {
	override fun toString(): String {
		return "Greeting(content='$name')"
	}
}