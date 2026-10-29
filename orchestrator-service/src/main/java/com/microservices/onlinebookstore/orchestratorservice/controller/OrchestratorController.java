package com.microservices.onlinebookstore.orchestratorservice.controller;

import com.microservices.onlinebookstore.orchestratorservice.dto.OrderRequest;
import com.microservices.onlinebookstore.orchestratorservice.dto.OrderResponse;
import com.microservices.onlinebookstore.orchestratorservice.service.OrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequiredArgsConstructor
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest,
                                              @AuthenticationPrincipal Jwt jwt) {
        OrderResponse orderResponse = orchestratorService.createOrder(orderRequest, jwt.getTokenValue());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderResponse);
    }

}
