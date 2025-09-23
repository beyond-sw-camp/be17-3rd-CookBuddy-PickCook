package org.example.be17pickcook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "test03";
    }

    @GetMapping("/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok().body("ok");
    }
}
