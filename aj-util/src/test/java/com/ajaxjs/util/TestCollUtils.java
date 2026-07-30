package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TestCollUtils {
    @Test
    void intList2ArrConvertsListToIntArray() {
        assertArrayEquals(
                new int[]{1, 2, 3},
                ConvertBasicValue.intList2Arr(Arrays.asList(1, 2, 3))
        );
    }

    @Test
    void stringArr2intArrConvertsStringToIntArray() {
        assertArrayEquals(
                new int[]{1, 2, 3},
                ConvertBasicValue.stringArr2intArr("1,2,3")
        );
    }
}
