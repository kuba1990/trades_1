package com.verygoodbank.tes.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.verygoodbank.tes.domain.ProductCsv;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class ProductService {

    private Map<Integer, String> productMap;

    @PostConstruct
    public void init() throws IOException {
        List<ProductCsv> productCsvs = new CsvToBeanBuilder<ProductCsv>(new FileReader("src/main/resources/product.csv"))
                .withType(ProductCsv.class)
                .build()
                .parse();

        for (ProductCsv productCsv : productCsvs) {
            productMap.put(productCsv.getProductId(), productCsv.getProductName());
        }
    }

    String getProductName(int productId) {
        return productMap.getOrDefault(productId, "Missing Product Name");
    }
}