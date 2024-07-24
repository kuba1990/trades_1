package com.verygoodbank.tes.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.verygoodbank.tes.domain.Order;
import com.verygoodbank.tes.domain.Status;
import com.verygoodbank.tes.db.OrderRepository;
import com.verygoodbank.tes.db.TradeRepository;
import com.verygoodbank.tes.domain.TradeCsv;
import com.verygoodbank.tes.domain.TradeDto;
import com.verygoodbank.tes.exception.NotReadyException;
import com.verygoodbank.tes.port.GenerateCSV;
import com.verygoodbank.tes.port.ProcessCSV;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.verygoodbank.tes.domain.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService implements GenerateCSV, ProcessCSV {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private final ProductService productService;

    private final OrderRepository orderRepository;

    private final TradeRepository tradeRepository;

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Void> startProcessCommand(final MultipartFile file, final String identifier) {
        return processTrades(file)
                .thenAcceptAsync(futures -> saveResult(futures, identifier))
                .exceptionally(e -> {
                    log.error("Error during processing process {} ", identifier, e);
                    return null;
                });
    }

    public CompletableFuture<List<CompletableFuture<TradeCsv>>> processTrades(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<TradeCsv> trades = new CsvToBeanBuilder<TradeCsv>(reader)
                    .withType(TradeCsv.class)
                    .build()
                    .parse();

            List<CompletableFuture<TradeCsv>> futures = trades.stream()
                    .filter(this::isValidDate)
                    .map(tradeCsv -> CompletableFuture.supplyAsync(() -> {
                        enrichTrade(tradeCsv);
                        return tradeCsv;
                    }))
                    .toList();

            return CompletableFuture.completedFuture(futures);
        } catch (IOException e) {
            log.error("Error during processing file", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional
    @Override
    public String processTrades(String identifier) {
        if (!orderRepository.isReady(identifier)) {
            throw new NotReadyException();
        }
        List<TradeDto> trades = tradeRepository.findAllByIdentifier(identifier).stream()
                .map(entity -> new TradeDto(
                        dateFormat.format(entity.getDate()),
                        entity.getProductName(),
                        entity.getCurrency(),
                        entity.getPrice()
                )).toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("date,product_name,currency,price");

        // Write CSV rows
        for (TradeDto trade : trades) {
            writer.println(String.format("%s,%s,%s,%s",
                    trade.date(),
                    trade.productName(),
                    trade.currency(),
                    trade.price()));
        }

        writer.flush();
        return out.toString(StandardCharsets.UTF_8);
    }

    private void saveResult(List<CompletableFuture<TradeCsv>> process, @NonNull final String identifier) {
        Order order = Order.builder()
                .identifier(identifier)
                .build();
        int batchSize = 100;
        Set<Product> batch = new HashSet<>();
        for (CompletableFuture<TradeCsv> tradeFuture : process) {
            try {
                TradeCsv tradeCsv = tradeFuture.join();
                Product productEntity = convertToTradeEntity(tradeCsv, identifier);
                batch.add(productEntity);
                order.setProducts(batch);

                if (batch.size() == batchSize) {
                    order.setProducts(batch);
                    orderRepository.save(order);
                    batch.clear();
                }
            } catch (Exception e) {
                log.error("Error during save data with ID: {}", identifier, e);
            }
        }

        if (!batch.isEmpty()) {
            order.setProducts(batch);
            orderRepository.save(order);
        }
        order.setStatus(Status.FINISHED);
        orderRepository.save(order);
    }

    private boolean isValidDate(TradeCsv tradeCsv) {
        try {
            dateFormat.parse(tradeCsv.getDate());
            return true;
        } catch (ParseException e) {
            log.error("Error during parsing file", e);
            return false;
        }
    }

    private void enrichTrade(TradeCsv tradeCsv) {
        String productName = productService.getProductName(tradeCsv.getProductId());
        tradeCsv.setProductName(productName);
    }

    @SneakyThrows
    private Product convertToTradeEntity(@NonNull TradeCsv tradeCsv, @NonNull final String identifier) {
        return Product.builder()
                .date(dateFormat.parse(tradeCsv.getDate()))
                .productName(tradeCsv.getProductName())
                .price(tradeCsv.getPrice())
                .identifier(identifier)
                .currency(tradeCsv.getCurrency())
                .build();
    }
}