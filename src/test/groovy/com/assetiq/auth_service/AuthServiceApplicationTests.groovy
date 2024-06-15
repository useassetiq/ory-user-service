package com.assetiq.auth_service

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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
}
