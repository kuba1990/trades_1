package com.verygoodbank.tes.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TradeCsv {
    @CsvBindByName(column = "date")
    private String date;

    @CsvBindByName(column = "product_id")
    private int productId;

    @CsvBindByName(column = "currency")
    private String currency;

    @CsvBindByName(column = "price")
    private double price;

    @Setter
    private String productName;
}