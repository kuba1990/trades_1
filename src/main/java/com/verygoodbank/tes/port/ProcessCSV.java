package com.verygoodbank.tes.port;


import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface ProcessCSV {

    CompletableFuture<Void> startProcessCommand(final MultipartFile file, final String identifier);
}
