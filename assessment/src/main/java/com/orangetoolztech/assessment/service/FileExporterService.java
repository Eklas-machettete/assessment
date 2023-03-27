package com.orangetoolztech.assessment.service;

import com.orangetoolztech.assessment.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class FileExporterService {
    private static final Logger logger = LoggerFactory.getLogger(FileExporterService.class);
    private final TaskExecutor taskExecutor;
    private final EntityManager entityManager;
    int BATCH_SIZE = 100000;
    long endTime;
    @Value("${fileExport.path}")
    String fileExportPath;

    public FileExporterService(TaskExecutor taskExecutor, EntityManager entityManager) {
        this.taskExecutor = taskExecutor;
        this.entityManager = entityManager;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        System.out.println(startTime);
        int totalRecords = entityManager.createQuery("SELECT COUNT(e) FROM Customer e", Long.class).getSingleResult().intValue();
        int totalPages = (int) Math.ceil((double) totalRecords / BATCH_SIZE);
        for (int page = 0; page < totalPages; page++) {
            int start = page * BATCH_SIZE;
            int end = Math.min(start + BATCH_SIZE, totalRecords);
            taskExecutor.execute(() -> writeToFile(start, end,startTime));
        }
        long endTime = System.currentTimeMillis();

    }

    private void writeToFile(int start, int end, long startTime) {
        Query query = entityManager.createQuery("SELECT e FROM Customer e");
        query.setFirstResult(start);
        query.setMaxResults(BATCH_SIZE);

        List<Customer> entities = query.getResultList();
        String fileName = fileExportPath+"file_" + start + "_" + end + ".txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            for (Customer entity : entities) {
                writer.write(entity.toString() + "\n");
            }
            endTime = System.currentTimeMillis();
            logger.info("Total time {}", (end - start));
            logger.info("Total exported time:"+ (endTime - startTime));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}