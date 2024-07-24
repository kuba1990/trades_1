package com.verygoodbank.tes.domain;

public record TradeDto(
        String date,
        String productName,
        String currency,
        double price
) {
}