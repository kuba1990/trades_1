package com.verygoodbank.tes.port;
import jakarta.transaction.Transactional;

public interface GenerateCSV {

    @Transactional
    String processTrades(String identifier);
}
