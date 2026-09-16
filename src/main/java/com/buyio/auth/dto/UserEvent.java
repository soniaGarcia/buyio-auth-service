package com.buyio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private String eventType; // e.g. USER_REGISTERED, USER_LOGGED_IN
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime timestamp;
}