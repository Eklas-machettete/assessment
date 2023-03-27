package com.orangetoolztech.assessment.controller;

import com.orangetoolztech.assessment.service.FileProcessingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/process_file")
public class FileProcessController {
    private final FileProcessingService fileProcessingService;


    public FileProcessController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping
    public String processFile() throws IOException, InterruptedException, ExecutionException {
        fileProcessingService.processTxtFile();
        return null;
    }


}
