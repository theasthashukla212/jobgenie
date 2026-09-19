package com.jobgenie.jobgenie_backend;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrationReturnsTokenWithoutPasswordAndDuplicateIsConflict() throws Exception {
        String email = "user-" + System.nanoTime() + "@example.com";
        String body = "{\"email\":\"" + email + "\",\"password\":\"Password1\",\"firstName\":\"Test\",\"lastName\":\"User\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void invalidRegistrationAndLoginAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"Password1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void jobsArePublicButUserOwnedResourcesRequireValidJwt() throws Exception {
        mockMvc.perform(get("/api/jobs").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        mockMvc.perform(get("/api/resumes").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotManageJobs() throws Exception {
        String email = "user-" + System.nanoTime() + "@example.com";
        String registration = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password1\",\"firstName\":\"Test\",\"lastName\":\"User\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(registration);

        mockMvc.perform(post("/api/jobs").header("Authorization", "Bearer " + response.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Engineer\",\"company\":\"Acme\",\"location\":\"Remote\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("permission")));
    }

    @Test
    void refreshTokensRotateAndCannotBeReused() throws Exception {
        String email = "refresh-" + System.nanoTime() + "@example.com";
        String registration = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password1\",\"firstName\":\"Refresh\",\"lastName\":\"User\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(registration).get("refreshToken").asText();

        String rotated = mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn().getResponse().getContentAsString();
        org.hamcrest.MatcherAssert.assertThat(objectMapper.readTree(rotated).get("refreshToken").asText(),
                org.hamcrest.Matchers.not(refreshToken));

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void multipartResumeUploadIsOwnedAndDownloadable() throws Exception {
        String email = "resume-" + System.nanoTime() + "@example.com";
        String registration = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password1\",\"firstName\":\"Resume\",\"lastName\":\"User\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(registration).get("token").asText();
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "Candidate resume".getBytes());

        String resume = mockMvc.perform(multipart("/api/resumes/upload")
                        .file(file).param("title", "Uploaded Resume").param("isDefault", "true")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filePath").isString())
                .andReturn().getResponse().getContentAsString();
        long resumeId = objectMapper.readTree(resume).get("id").asLong();

        mockMvc.perform(get("/api/resumes/" + resumeId + "/file")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Candidate resume"));
    }

    @Test
    void openApiDocumentIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("JobGenie API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }
}
