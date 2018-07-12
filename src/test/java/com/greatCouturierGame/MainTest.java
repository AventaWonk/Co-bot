package com.greatCouturierGame;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private static final String TEST_ACCOUNTS_FILE_NAME = "temp.accounts.txt";

    @Test
    void getAccounts() {
        final String testAccountPartsSeparator = ":";
        Map<String, String> testAccounts = new HashMap<>();
        testAccounts.put("1233123","5432112345567890987667890567890");
        testAccounts.put("72331223","6432112345567890987667890567899");
        testAccounts.put("312233123","df3211234556789098766789056782d");

        try (FileWriter fw = new FileWriter(MainTest.TEST_ACCOUNTS_FILE_NAME)) {
            BiConsumer<String,String> writeFunction = (id, token) -> {
                try {
                    fw.write(id + testAccountPartsSeparator + token + '\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            testAccounts.forEach(writeFunction);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File testAccountsFile = new File(MainTest.TEST_ACCOUNTS_FILE_NAME);
        assertTrue(testAccountsFile.exists());
        Map<String, String> accounts = null;
        try {
            accounts = Main.getAccounts(
                    new File(MainTest.TEST_ACCOUNTS_FILE_NAME),
                    testAccountPartsSeparator,
                    false
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotNull(accounts);
        assertEquals(testAccounts, accounts);
        assertTrue(new File(MainTest.TEST_ACCOUNTS_FILE_NAME).delete());
    }

    @Test
    void isUserIdValid() {
        List<String> validIdsList = Arrays.asList(
                "343",
                "234334",
                "12334233"
        );
        List<String> invalidIdsList = Arrays.asList(
                "2324f323",
                "-124578",
                ":353",
                "3:679"
        );

        validIdsList.forEach((token) -> {
            assertTrue(Main.isUserIdValid(token));
        });
        invalidIdsList.forEach((token) -> {
            assertFalse(Main.isUserIdValid(token));
        });
    }

    @Test
    void isUserAuthTokenValid() {
        List<String> validTokensList = Arrays.asList(
                "5d321123455678909q76678905678999",
                "ad321123455678909q76678905678999",
                "ad321123455678909q76678905678bcd"
        );
        List<String> invalidTokensList = Arrays.asList(
                "d321/823455678909q76678905678999",
                "@d321123455678909q76678905678999",
                ":321123455678909q76678905678bc7d",
                "a32118923455678909q76678905678bcd",
                "a321123455678909q76678905678bcd"
        );

        validTokensList.forEach((token) -> {
            assertTrue(Main.isUserAuthTokenValid(token));
        });
        invalidTokensList.forEach((token) -> {
            assertFalse(Main.isUserAuthTokenValid(token));
        });
    }

}