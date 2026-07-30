package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TestObjectHelper {
    @Test
    void textAndEmptyChecksCoverNullBlankAndContent() {
        assertFalse(ObjectHelper.hasText(null));
        assertFalse(ObjectHelper.hasText(""));
        assertFalse(ObjectHelper.hasText(" \t\n"));
        assertTrue(ObjectHelper.hasText("  value  "));

        assertTrue(ObjectHelper.isEmpty((Object[]) null));
        assertTrue(ObjectHelper.isEmpty(new Object[0]));
        assertTrue(ObjectHelper.isEmpty(Collections.emptyList()));
        assertTrue(ObjectHelper.isEmpty(Collections.emptyMap()));
    }

    @Test
    void collectionFactoriesReturnExpectedImmutableValues() {
        Map<String, Integer> map = ObjectHelper.mapOf("a", 1, "b", 2, "c", 3);
        List<String> list = ObjectHelper.listOf("a", "b");
        Set<String> set = ObjectHelper.setOf("a", "b", "a");

        assertEquals(ObjectHelper.mapOf("a", 1, "b", 2, "c", 3), map);
        assertEquals(java.util.Arrays.asList("a", "b"), list);
        assertEquals(2, set.size());
        assertThrows(UnsupportedOperationException.class, () -> list.add("c"));
        assertThrows(UnsupportedOperationException.class, () -> set.add("c"));
        assertThrows(IllegalArgumentException.class, () -> ObjectHelper.setOf("a", null));
    }

    @Test
    void initialCapacityDoesNotOverflowForLargeExpectedSize() {
        assertEquals(16, ObjectHelper.getInitialCapacity(0));
        assertEquals(16, ObjectHelper.getInitialCapacity(12));
        assertEquals(32, ObjectHelper.getInitialCapacity(13));
        assertEquals(1 << 30, ObjectHelper.getInitialCapacity(Integer.MAX_VALUE));
    }
}
