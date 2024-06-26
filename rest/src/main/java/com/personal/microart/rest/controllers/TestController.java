package com.personal.microart.rest.controllers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.microart.rest.Endpoints.TEST_JWT;

/**
 * A controller that handles test requests.
 */
@Profile("test")
@RestController
@RequiredArgsConstructor
public class TestController extends BaseController {
    private final ExchangeAccessor exchangeAccessor;

    @PostConstruct
    private void setExchangeAccessor() {
        super.setExchangeAccessor(exchangeAccessor);
    }

    //Should return hello world. Test whether a jwt auth is ok
    @GetMapping(path = TEST_JWT)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> register() {
        return ResponseEntity.ok("Hello World");
    }
}
