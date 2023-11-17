package com.personal.mavrep.controllers;

import com.personal.mavrep.persistence.FileReader;
import com.personal.mavrep.persistence.FileSaver;
import com.personal.mavrep.exceptions.VersionAlreadyDeployedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final FileSaver fileSaver;
    private final FileReader fileReader;

    @GetMapping(path = "/mvn/**",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> get(HttpServletRequest request){

        byte[] bytes = this.fileReader.readFile(request.getRequestURI());

        return ResponseEntity.ok()
                .body(bytes);
    }

    @PutMapping(path = "/mvn/**")
    public ResponseEntity<String> put(@RequestBody byte[] bytes, HttpServletRequest request) {

        this.fileSaver.saveFileToDisk(request.getRequestURI(), bytes);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

//    @PutMapping(path = "/mvn/**",consumes = MediaType.ALL_VALUE)
//    public Callable<ResponseEntity<String>> put(){
//        return () -> new ResponseEntity<>(HttpStatus.CREATED);
//    }
}
