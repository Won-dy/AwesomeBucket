package com.example.awesomebucket.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoInputDataException extends RuntimeException {
    public NoInputDataException(String message) {
        super(message);
    }
}
