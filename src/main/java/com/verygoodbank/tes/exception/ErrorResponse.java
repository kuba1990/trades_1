package com.verygoodbank.tes.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ErrorResponse {

    private int status;

    private String error;
}