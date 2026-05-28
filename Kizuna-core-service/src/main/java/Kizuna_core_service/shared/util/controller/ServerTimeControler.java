package Kizuna_core_service.shared.util.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/server-time")
public class ServerTimeControler {

    @GetMapping()
    public ResponseEntity<String> getServerTime() {
        return ResponseEntity.ok(Instant.now().toString());
    }
}
