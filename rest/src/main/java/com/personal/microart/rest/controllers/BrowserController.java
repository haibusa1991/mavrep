package com.personal.microart.rest.controllers;

import com.personal.microart.api.operations.browse.BrowseInput;
import com.personal.microart.core.processor.browse.BrowseCore;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/browse")
public class BrowserController extends BaseController {

    private final BrowseCore browse;
//    private final ExchangeAccessor exchangeAccessor;
//
//    @PostConstruct
//    private void setExchangeAccessor() {
//        this.setExchangeAccessor(exchangeAccessor);
//    }

    @GetMapping(path = "/**")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request, HttpServletResponse response) {

        BrowseInput input = BrowseInput
                .builder()
                .uri(request.getRequestURI())
                .build();

        return this.handle(this.browse.process(input), response);
    }

}
