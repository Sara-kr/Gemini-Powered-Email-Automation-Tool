package com.email.email_writer_sb.app;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> generateEmail(
            @RequestBody EmailRequest emailRequest) {

        return emailGeneratorService.generateEmailReply(emailRequest)
                .map(ResponseEntity::ok);
    }
}
