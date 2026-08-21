package com.ajaxjs.qr_code.fast;

import com.ajaxjs.qr_code.DataTooLongException;
import com.ajaxjs.qr_code.Ecc;
import com.ajaxjs.qr_code.Mode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TestQrCodeFast {
    @Test
    void bitBufferSupportsScalarAndArrayAppends() {
        BitBuffer bits = new BitBuffer();
        bits.appendBits(0b101, 3);
        bits.appendBits(new int[]{0xC0000000}, 2);
        assertEquals(1, bits.getBit(0));
        assertEquals(5, bits.bitLength);
        assertThrows(IllegalStateException.class, bits::getBytes);
        bits.appendBits(0, 3);
        assertEquals(1, bits.getBytes().length);
        assertThrows(IndexOutOfBoundsException.class, () -> bits.getBit(8));
        assertThrows(IllegalArgumentException.class, () -> bits.appendBits(8, 3));
    }

    @Test
    void fastSegmentsCoverModesTotalsAndValidation() {
        assertEquals(Mode.BYTE, QrSegmentFast.makeBytes(new byte[]{1}).mode);
        assertEquals(Mode.NUMERIC, QrSegmentFast.makeNumeric("123").mode);
        assertEquals(Mode.ALPHANUMERIC, QrSegmentFast.makeAlphanumeric("HELLO").mode);
        assertEquals(Mode.ECI, QrSegmentFast.makeEci(26).mode);
        assertTrue(QrSegmentFast.isNumeric("123"));
        assertTrue(QrSegmentFast.isAlphanumeric("ABC"));
        assertEquals(24, QrSegmentFast.getTotalBits(QrSegmentFast.makeSegments("123"), 1));
        assertThrows(IllegalArgumentException.class, () -> QrSegmentFast.makeNumeric("A"));
        assertThrows(IllegalArgumentException.class, () -> QrSegmentFast.makeEci(-1));
    }

    @Test
    void fastAdvancedSegmentationHandlesUnicodeAndKanji() {
        int[] codePoints = QrSegmentAdvancedFast.toCodePoints("A😀");
        assertArrayEquals(new int[]{'A', 0x1F600}, codePoints);
        Mode[] modes = QrSegmentAdvancedFast.computeCharacterModes(codePoints, 1);
        assertEquals(2, modes.length);
        assertFalse(QrSegmentAdvancedFast.splitIntoSegments(codePoints, modes).isEmpty());
        assertFalse(QrSegmentAdvancedFast.makeSegmentsOptimally(codePoints, 1).isEmpty());
        assertEquals(Mode.KANJI, QrSegmentAdvancedFast.makeKanji("漢字").mode);
        assertFalse(QrSegmentAdvancedFast.makeSegmentsOptimally("ABC123中文", Ecc.LOW, 1, 10).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> QrSegmentAdvancedFast.makeKanji("A"));
    }

    @Test
    void memoizerCachesResults() {
        AtomicInteger calls = new AtomicInteger();
        Memoizer<String, String> memoizer = new Memoizer<>(value -> {
            calls.incrementAndGet();
            return value.toUpperCase();
        });
        assertEquals("ABC", memoizer.get("abc"));
        assertEquals("ABC", memoizer.get("abc"));
        assertEquals(1, calls.get());
    }

    @Test
    void templateGeneratesPatternsMasksAndScanIndexes() {
        QrTemplate template = QrTemplate.MEMOIZER.get(7);
        assertSame(template, QrTemplate.MEMOIZER.get(7));
        assertEquals(8, template.masks.length);
        assertTrue(template.dataOutputBitIndexes.length > 0);
        assertArrayEquals(new int[]{6, 22, 38}, template.getAlignmentPatternPositions());
        assertTrue(template.getModule(template.template, 0, 0) >= 0);
        assertEquals(208, QrTemplate.getNumRawDataModules(1));
        assertThrows(IllegalArgumentException.class, () -> QrTemplate.MEMOIZER.get(0));
    }

    @Test
    void reedSolomonGeneratorCalculatesRemainder() {
        ReedSolomonGenerator generator = ReedSolomonGenerator.MEMOIZER.get(7);
        byte[] result = new byte[7];
        generator.getRemainder(new byte[]{1, 2, 3}, 0, 3, result);
        assertFalse(Arrays.equals(new byte[7], result));
        assertThrows(IllegalArgumentException.class, () -> ReedSolomonGenerator.MEMOIZER.get(0));
    }

    @Test
    void fastEncoderSupportsTextBinarySegmentsMasksAndBounds() {
        QrCodeFast text = QrCodeFast.encodeText("Hello 世界", Ecc.MEDIUM);
        QrCodeFast binary = QrCodeFast.encodeBinary(new byte[]{0, 1, 2}, Ecc.LOW);
        QrCodeFast forced = QrCodeFast.encodeSegments(QrSegmentFast.makeSegments("HELLO"),
                Ecc.HIGH, 1, 10, 3, true);
        assertTrue(text.size >= 21);
        assertTrue(binary.getModule(0, 0));
        assertEquals(3, forced.mask);
        assertFalse(text.getModule(-1, -1));
        assertEquals(19, QrCodeFast.getNumDataCodewords(1, Ecc.LOW));
        assertEquals(1, QrCodeFast.getBit(2, 1));
        assertThrows(IllegalArgumentException.class,
                () -> QrCodeFast.encodeSegments(Collections.emptyList(), Ecc.LOW, 0, 1, -1, true));
        assertThrows(DataTooLongException.class,
                () -> QrCodeFast.encodeSegments(Collections.singletonList(QrSegmentFast.makeBytes(new byte[100])),
                        Ecc.HIGH, 1, 1, -1, false));
    }

    @Test
    void fastPenaltyAndMaskHelpersAreCallable() {
        QrCodeFast qr = QrCodeFast.encodeText("MASK", Ecc.LOW);
        assertTrue(qr.getPenaltyScore() >= 0);
        int[] history = new int[7];
        qr.finderPenaltyAddHistory(2, history);
        assertTrue(history[0] >= 2);
        assertTrue(qr.finderPenaltyTerminateAndCount(0, 1, history) >= 0);
        assertTrue(qr.finderPenaltyCountPatterns(history) >= 0);
        int[] mask = QrTemplate.MEMOIZER.get(qr.version).masks[0];
        qr.applyMask(mask);
        qr.applyMask(mask);
        assertTrue(qr.handleConstructorMasking(QrTemplate.MEMOIZER.get(qr.version).masks, 0) >= 0);
    }
}
