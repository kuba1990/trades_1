package com.verygoodbank.tes;

import com.verygoodbank.tes.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradeEnrichmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnrichTradeData_Upload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                MediaType.TEXT_PLAIN_VALUE, "date,productName,currency,price\n20230101,ProductA,USD,100.0".getBytes());

        when(tradeService.startProcessCommand(file, "testIdentifier")).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(multipart("/api/v1/enrich")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void testEnrichTradeData_downloadData() throws Exception {
        String identifier = UUID.randomUUID().toString();

        when(tradeService.processTrades(identifier)).thenReturn("date,product_name,currency,price\n20230101,ProductA,USD,100.0");

        mockMvc.perform(get("/api/v1/enrich")
                        .param("identifier", identifier))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=trades.csv"))
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ProductA")));
    }
}

