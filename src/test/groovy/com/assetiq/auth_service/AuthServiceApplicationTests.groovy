/* (C) 2024 */
package com.assetiq.auth_service

import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import sh.ory.kratos.model.VerifiableIdentityAddress

import java.time.OffsetDateTime

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

	def mapper = new ObjectMapper()
	def payload = new String(this.getClass().getResourceAsStream("/payload.json").readAllBytes())

	def setupIdentity(String email, CreateIdentityBody.StateEnum state, boolean  verified) {

		CreateIdentityBody createIdentityBody = new CreateIdentityBody()
		def consents = Map.of(
				"newsletter", false,
				"terms", false,
				"privacy", true,
				"marketing", true
				)
		Map<String, Object> traits = Map.of(
				"email", email,
				"firstName", "Admin",
				"lastName", "Ory",
				"consents", consents)

		def verifiableAddress = new VerifiableIdentityAddress()
				.verified(verified)
				.value(traits["email"] as String)
				.createdAt(OffsetDateTime.now())
				.updatedAt(OffsetDateTime.now())
		if (verified) {
			verifiableAddress.setVerifiedAt(OffsetDateTime.now())
			verifiableAddress.setStatus("completed")
			verifiableAddress.setVia(VerifiableIdentityAddress.ViaEnum.EMAIL)
		}

		createIdentityBody.schemaId("default")
				.addVerifiableAddressesItem(verifiableAddress)
				.state(state)
				.traits(traits)

		return identityApi.createIdentity(createIdentityBody)
	}

	def deleteIdentity(String id) {
		identityApi.deleteIdentity(id)
	}

	def "test put user profile"() {
		setup: "a user id and a user profile"
			Identity identity = setupIdentity("admin1@ory.sh", CreateIdentityBody.StateEnum.ACTIVE, true)

		when: "a put request is sent to the /user-profile endpoint"
			def result = mockMvc.perform(MockMvcRequestBuilders.put("/user-profile")
				.header("X-User-Id", identity.id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))

		then: "the response status is 204 No Content"
			result.andExpect(status().isNoContent())
		when:
			def updatedIdentity = identityApi.getIdentity(identity.id, List.of())
		then:
			mapper.convertValue(updatedIdentity.metadataPublic, Map)
					== mapper.readValue(payload, Map)
		cleanup:
			deleteIdentity(identity.id)

	}

	def "test for user not found"() {
		
		when: "a put request is sent to the /user-profile endpoint"
			def result = mockMvc.perform(MockMvcRequestBuilders.put("/user-profile")
				.header("X-User-Id", "not_found_id")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))

		then: "the response status is 404 No Found"
			result.andExpect(status().isNotFound())
			result.andExpect(jsonPath("\$.code", is("identity_not_found")))
			result.andExpect(jsonPath("\$.message", is("Identity with id 'not_found_id' not found")))
	}

	def "test put user profile with an inactive account"() {
		setup: "a user id and a user profile"
			Identity identity = setupIdentity("admin2@ory.sh", CreateIdentityBody.StateEnum.INACTIVE, true)

		when: "a put request is sent to the /user-profile endpoint"
			def result = mockMvc.perform(MockMvcRequestBuilders.put("/user-profile")
				.header("X-User-Id", identity.id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))

		then: "the response status is bad request"
			result.andExpect(status().isBadRequest())
			result.andExpect(jsonPath("\$.code", is("identity_inactive")))

		then: "the response status is bad request"
			result.andExpect(status().isBadRequest())
			result.andExpect(jsonPath("\$.code", is("identity_inactive")))
	}

	def "test put user profile with a non verified account"() {
		setup: "a user id and a user profile"
		Identity identity = setupIdentity("admin3@ory.sh", CreateIdentityBody.StateEnum.ACTIVE, false)

		when: "a put request is sent to the /user-profile endpoint"
		def result = mockMvc.perform(MockMvcRequestBuilders.put("/user-profile")
				.header("X-User-Id", identity.id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))

		then: "the response status is bad request"
		result.andExpect(status().isBadRequest())
		result.andExpect(jsonPath("\$.code", is("identity_inactive")))

		then: "the response status is bad request"
			result.andExpect(status().isBadRequest())
			result.andExpect(jsonPath("\$.code", is("identity_inactive")))
	}
}
