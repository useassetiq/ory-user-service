/* (C) 2024 */
package com.assetiq.auth_service

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.assetiq.accounts.AuthServiceApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import sh.ory.kratos.api.IdentityApi
import sh.ory.kratos.model.CreateIdentityBody
import sh.ory.kratos.model.Identity
import spock.lang.Specification

@SpringBootTest(classes = [AuthServiceApplication.class])
@AutoConfigureMockMvc
class AuthServiceApplicationTests extends Specification implements KratosFixture {

	@Autowired
	MockMvc mockMvc

	@Autowired
	IdentityApi identityApi

	Identity identity

	def setup() {

		CreateIdentityBody createIdentityBody = new CreateIdentityBody()
		def consents = Map.of(
				"newsletter", false,
				"terms", false,
				"privacy", true,
				"marketing", true
				)
		Map<String, Object> traits = Map.of(
				"email", "admin@ory.sh",
				"firstName", "Admin",
				"lastName", "Ory",
				"consents", consents)

		createIdentityBody.schemaId("default")
				.state(CreateIdentityBody.StateEnum.ACTIVE)
				.traits(traits)

		identity = identityApi.createIdentity(createIdentityBody)
	}

	def "test put user profile"() {
		given: "a user id and a user profile"
		String userProfileJson = "{ \"name\": \"Test User\", \"email\": \"test@example.com\" }"

		when: "a put request is sent to the /user-profile endpoint"
		def result = mockMvc.perform(MockMvcRequestBuilders.put("/user-profile")
				.header("X-User-Id", identity.id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(userProfileJson))

		then: "the response status is 204 No Content"
		result.andExpect(status().isNoContent())
	}

	def "test for user not found"() {
		given: "a user id and a user profile"
		String userProfileJson = "{ \"name\": \"Test User\", \"email\": \"test@example.com\" }"

		when: "a put request is sent to the /user-profile endpoint"
		def result = mockMvc.perform(MockMvcRequestBuilders.put("/user-profile")
				.header("X-User-Id", "not_found_id")
				.contentType(MediaType.APPLICATION_JSON)
				.content(userProfileJson))

		then: "the response status is 404 No Found"
		result.andExpect(status().isNotFound())
		result.andExpect(jsonPath("\$.code", is("identity_not_found")))
		result.andExpect(jsonPath("\$.message", is("Identity with id 'not_found_id' not found")))
	}
}
