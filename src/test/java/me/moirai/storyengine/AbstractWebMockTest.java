package me.moirai.storyengine;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public abstract class AbstractWebMockTest {

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final int PORT = 1551;

    protected static WireMockServer wireMockServer;
    protected static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {

        objectMapper = new ObjectMapper();

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .port(PORT)
                .http2PlainDisabled(true));

        wireMockServer.start();
    }

    @AfterAll
    static void tearDown() {

        wireMockServer.stop();
    }

    protected void prepareWebserverFor(Object response, int httpStatus) throws JsonProcessingException {

        wireMockServer.stubFor(any(anyUrl())
                .willReturn(aResponse()
                        .withStatus(httpStatus)
                        .withBody(objectMapper.writeValueAsString(response))
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)));
    }

    protected void prepareWebserverFor(Object response, HttpStatus httpStatus) throws JsonProcessingException {

        prepareWebserverFor(response, httpStatus.value());
    }

    protected void prepareWebserverFor(int httpStatus) {

        wireMockServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withStatus(httpStatus)));
    }

    protected void prepareWebserverFor(HttpStatus httpStatus) {

        prepareWebserverFor(httpStatus.value());
    }
}
