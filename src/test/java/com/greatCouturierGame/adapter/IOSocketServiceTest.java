package com.greatCouturierGame.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IOSocketServiceTest {

    @Test
    void concatArrays() {
        byte[] arr1 = {0, 1, 2};
        byte[] arr2 = {3, 4};
        
        byte[] actualArr = IOSocketService.concatArrays(arr1, arr2);
        byte[] expectedArr = {0, 1, 2, 3, 4};
        assertEquals(expectedArr, actualArr);

        arr1 = new byte[]{0};
        arr2 = new byte[]{1};
        actualArr = IOSocketService.concatArrays(arr1, arr2);
        expectedArr = new byte[]{0, 1};
        assertEquals(expectedArr, actualArr);
    }
}