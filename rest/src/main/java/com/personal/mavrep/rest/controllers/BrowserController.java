package com.personal.mavrep.rest.controllers;

import com.personal.mavrep.api.operations.browse.BrowseInput;
import com.personal.mavrep.core.processor.browse.BrowseCore;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/browse")
public class BrowserController extends BaseController {

    private final BrowseCore browse;

    @GetMapping(path = "/**")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request, HttpServletResponseImpl response) {

        BrowseInput input = BrowseInput
                .builder()
                .uri(request.getRequestURI())
                .build();

        return this.handle(this.browse.process(input), response);
    }

}
