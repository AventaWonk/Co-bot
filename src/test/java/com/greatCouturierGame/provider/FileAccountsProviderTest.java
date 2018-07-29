package com.greatCouturierGame.provider;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileAccountsProviderTest {

    private static Map<String, String> testAccountsMap = new HashMap<>();

    static {
        testAccountsMap.put("1233123","5432112345567890987667890567890");
        testAccountsMap.put("72331223","6432112345567890987667890567899");
        testAccountsMap.put("312233123","df3211234556789098766789056782d");
    }

    private void createTestAccountsFile(String fileName, String separator) throws IOException {
        try (FileWriter fw = new FileWriter(fileName)) {
            for (Map.Entry<String, String> entry : testAccountsMap.entrySet()) {
                fw.write(entry.getKey() + separator + entry.getValue() +'\n');
            }
        }
    }

    private void deleteTestAccountsFile(String fileName) {
        new File(fileName).delete();
    }

    @Test
    void getAccounts() throws IOException {
        final String fileName = "temp.accounts.txt";
        final String separator = ":";
        this.createTestAccountsFile(fileName, separator);

        FileAccountsProvider accountsProvider = new FileAccountsProvider(fileName, separator);
        Map<String, String> accountsMap = accountsProvider.getAccounts();
        assertEquals(testAccountsMap, accountsMap);
        deleteTestAccountsFile(fileName);
    }

}