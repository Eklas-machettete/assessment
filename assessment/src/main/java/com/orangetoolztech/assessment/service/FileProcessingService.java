package com.orangetoolztech.assessment.service;

import com.google.common.collect.Lists;
import com.orangetoolztech.assessment.entity.Customer;
import com.orangetoolztech.assessment.entity.InvalidateCustomer;
import com.orangetoolztech.assessment.repository.CustomerRepository;
import com.orangetoolztech.assessment.repository.InvalidateCustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class FileProcessingService {
    private final CustomerRepository customerRepository;
    private final InvalidateCustomerRepository invalidateCustomerRepository;
    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);

    static HashSet<String> emailHashSet = new HashSet<String>();
    static HashSet<String> phoneNumberHashSet = new HashSet<String>();
    static Set<Customer> customerSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    static Set<InvalidateCustomer> invalidateCustomerSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @Value("${file.path}")
    String filePath;
    public FileProcessingService(CustomerRepository customerRepository, InvalidateCustomerRepository invalidateCustomerRepository, TaskExecutor taskExecutor) {
        this.customerRepository = customerRepository;
        this.invalidateCustomerRepository = invalidateCustomerRepository;

    }
    public void processTxtFile() throws IOException, InterruptedException {
        int numThreads = 4;
        long chunkSize = new java.io.File(filePath).length() / numThreads;

        try {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<ProcessingTask> tasks = new ArrayList<>();
            long start = 0, end = chunkSize;
            for (int i = 0; i < numThreads; i++) {
                if (i == numThreads - 1) {
                    end = Long.MAX_VALUE;
                }
                ProcessingTask task = new ProcessingTask(customerRepository, invalidateCustomerRepository, filePath, start, end);
                tasks.add(task);
                executor.submit(task);
                start = end + 1;
                end += chunkSize;
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw e;
        }

        logger.info("valid custome: " + customerSet.size() + "; invalide customer " + invalidateCustomerSet.size());
        List<Customer> customerList = new ArrayList<>(customerSet);
        //  customerRepository.saveAll(customerList);
        List<List<Customer>> batchesObjectCustomer = Lists.partition(customerList, 1000);
        for (List<Customer> batchObject : batchesObjectCustomer) {
            customerRepository.saveAll(batchObject);
        }
        List<InvalidateCustomer> invalidateCustomerList = new ArrayList<>(invalidateCustomerSet);
        // invalidateCustomerRepository.saveAll(invalidateCustomerList);
        List<List<InvalidateCustomer>> batchesObjectICustomer = Lists.partition(invalidateCustomerList, 1000);
        for (List<InvalidateCustomer> batchObject : batchesObjectICustomer) {
            invalidateCustomerRepository.saveAll(batchObject);
        }
    }


    static class ProcessingTask implements Runnable {
        private final CustomerRepository customerRepository;
        private final InvalidateCustomerRepository invalidateCustomerRepository;
        private final String filename;
        private final long start;
        private final long end;

        public ProcessingTask(CustomerRepository customerRepository, InvalidateCustomerRepository invalidateCustomerRepository, String filename, long start, long end) {
            this.customerRepository = customerRepository;
            this.invalidateCustomerRepository = invalidateCustomerRepository;
            this.filename = filename;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                reader.skip(start);
                String row;
                long endPointer = start;
                while ((row = reader.readLine()) != null && endPointer <= end) {
                    String finalRow = row;
                    String[] columnsTmp = finalRow.split(",");
                    int l = columnsTmp.length;
                    int i = 0;
                    String[] columns = new String[8];
                    for (; i <= 7; i++) {
                        if (i < l) columns[i] = columnsTmp[i];
                        else columns[i] = " ";
                    }
                    if (isValidate(columns[6], columns[5])) {

                        if (!isDuplicate(columns[6], columns[5])) {
                            Customer customer = new Customer();
                            customer.setName(columns[0]);
                            customer.setCol2(columns[2]);
                            customer.setCol1(columns[1]);
                            customer.setCol3(columns[3]);
                            customer.setCol4(columns[4]);
                            customer.setPhoneNumber(columns[5]);
                            customer.setEmail(columns[6]);
                            customer.setCol7(columns[7]);
                            customerSet.add(customer);
                        }
                    } else {
                        if (!isDuplicate(columns[6], columns[5])) {
                            InvalidateCustomer icustomer = new InvalidateCustomer();
                            icustomer.setEmail(columns[6]);
                            icustomer.setPhoneNumber(columns[5]);
                            icustomer.setName(columns[0]);
                            icustomer.setCol2(columns[2]);
                            icustomer.setCol1(columns[1]);
                            icustomer.setCol3(columns[3]);
                            icustomer.setCol7(columns[7]);
                            icustomer.setCol4(columns[4]);
                            invalidateCustomerSet.add(icustomer);
                        }
                    }
                    endPointer++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        boolean isDuplicate(String email, String phoneNumber) {
            boolean result = false;
            if (emailHashSet.contains(email) || phoneNumberHashSet.contains(phoneNumber)) result = true;
            if (!emailHashSet.contains(email)) emailHashSet.add(email);
            if (!phoneNumberHashSet.contains(phoneNumber)) phoneNumberHashSet.add(phoneNumber);
            return result;
        }

        boolean isValidate(String email, String phoneNumber) {
            return validatePhoneNumber(phoneNumber) && validateEmail(email);
        }

        public boolean validatePhoneNumber(String phoneNumber) {
            String regexMatchingForPhoneNumber = "^(1[- ]?)?(\\(\\d{3}\\)|\\d{3})[- ]?\\d{3}[- ]?\\d{4}$";
            return phoneNumber.matches(regexMatchingForPhoneNumber);
        }

        public boolean validateEmail(String email) {
            String regexMatchingForEmail = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@" + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$";
            return email.matches(regexMatchingForEmail);
        }
    }

}
