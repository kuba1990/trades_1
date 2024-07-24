package com.verygoodbank.tes;

import com.verygoodbank.tes.domain.TradeCsv;
import com.verygoodbank.tes.db.OrderRepository;
import com.verygoodbank.tes.db.TradeRepository;
import com.verygoodbank.tes.service.ProductService;
import com.verygoodbank.tes.service.TradeService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TradeServiceTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private OrderRepository orderRepository;

    private TradeService tradeService;

    private final static HashMap<Integer, String> product = new HashMap<>(Map.ofEntries(
            Map.entry(1, "Treasury Bills Domestic"),
            Map.entry(2, "Corporate Bonds Domestic"),
            Map.entry(3, "REPO Domestic"),
            Map.entry(4, "Interest rate swaps International"),
            Map.entry(5, "OTC Index Option"),
            Map.entry(6, "Currency Options"),
            Map.entry(7, "Reverse Repos International"),
            Map.entry(8, "REPO International"),
            Map.entry(9, "766A_CORP BD"),
            Map.entry(10, "766B_CORP BD")));

    @BeforeEach
    void setUp() {
        ProductService productService = new ProductService(product); // You can configure this if needed
        tradeService = new TradeService(productService, orderRepository, tradeRepository);
    }

    @Test
    void testProcessTrades_Success() throws Exception {
        //given
        String csvContent = readCsvFile("trade_test1.csv");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file = new MockMultipartFile("file", "trade_test1.csv", "text/csv", inputStream);

        //when
        CompletableFuture<List<CompletableFuture<TradeCsv>>> futures = tradeService.processTrades(file);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.get().toArray(new CompletableFuture[0]));
        allOf.join();

        List<TradeCsv> tradeCsvList = futures.get().stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        //then
        assertNotNull(tradeCsvList);
        assertEquals(4, tradeCsvList.size());


        assertTradeCsv(tradeCsvList.get(0), "20160101", 1, "EUR", 10.0, "Treasury Bills Domestic");
        assertTradeCsv(tradeCsvList.get(1), "20160101", 2, "EUR", 20.1, "Corporate Bonds Domestic");
        assertTradeCsv(tradeCsvList.get(2), "20160101", 3, "EUR", 30.34, "REPO Domestic");
        assertTradeCsv(tradeCsvList.get(3), "20160101", 11, "EUR", 35.34, "Missing Product Name");
    }

    @Test
    void testProcessTrades_IncorrectData() throws Exception {
        //given
        String csvContent = readCsvFile("trade_test2.csv");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file = new MockMultipartFile("file", "trade_test2.csv", "text/csv", inputStream);

        //when
        CompletableFuture<List<CompletableFuture<TradeCsv>>> futures = tradeService.processTrades(file);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.get().toArray(new CompletableFuture[0]));
        allOf.join();

        List<TradeCsv> tradeCsvList = futures.get().stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        //then
        assertNotNull(tradeCsvList);
        assertEquals(3, tradeCsvList.size());
    }

    private void assertTradeCsv(TradeCsv tradeCsv, String expectedDate, int expectedProductId, String expectedCurrency, double expectedPrice,
            String expectedProductName) {
        assertEquals(expectedDate, tradeCsv.getDate());
        assertEquals(expectedProductId, tradeCsv.getProductId());
        assertEquals(expectedCurrency, tradeCsv.getCurrency());
        assertEquals(expectedPrice, tradeCsv.getPrice());
        assertEquals(expectedProductName, tradeCsv.getProductName());
    }

    @SneakyThrows
    private String readCsvFile(final String fileName) {
        ClassPathResource resource = new ClassPathResource(fileName);
        byte[] content = resource.getInputStream().readAllBytes();
        return new String(content, StandardCharsets.UTF_8);
    }
}
