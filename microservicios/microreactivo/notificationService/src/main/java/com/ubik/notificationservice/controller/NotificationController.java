package com.ubik.notificationservice.controller;

import com.ubik.notificationservice.dto.NotificationRequest;
import com.ubik.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(
            @RequestHeader(value = "X-Internal-Request", required = false) String internalHeader,
            @Valid @RequestBody NotificationRequest request
    ) {

        //if (internalHeader == null || !internalHeader.equals("true")) {
        //    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        //}

        notificationService.sendEmail(request);
        return ResponseEntity.ok().build();
    }

}
