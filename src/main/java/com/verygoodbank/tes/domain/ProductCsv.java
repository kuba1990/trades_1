package com.verygoodbank.tes.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

@Getter
public class ProductCsv {
    @CsvBindByName(column = "product_id")
    private int productId;

    @CsvBindByName(column = "product_name")
    private String productName;

}
