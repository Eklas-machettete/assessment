package com.orangetoolztech.assessment.controller;

import com.orangetoolztech.assessment.service.FileExporterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/export_file")
public class FileExportController {
    private final FileExporterService fileProcessingService;

    public FileExportController(FileExporterService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }


    @GetMapping
    public String processFile() throws IOException, InterruptedException, ExecutionException {
        fileProcessingService.run();
        return null;
    }


}
