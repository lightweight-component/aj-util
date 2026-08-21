package com.ajaxjs.qr_code;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestQrCode {
    @TempDir
    Path tempDir;

    @Test
    void bitBufferAppendsReadsCopiesAndRejectsInvalidInput() {
        BitBuffer bits = new BitBuffer();
        bits.appendBits(0b101, 3);
        BitBuffer suffix = new BitBuffer();
        suffix.appendBits(0b11, 2);
        bits.appendData(suffix);

        assertEquals(5, bits.bitLength());
        assertEquals(1, bits.getBit(0));
        assertEquals(0, bits.getBit(1));
        BitBuffer copy = bits.clone();
        copy.appendBits(0, 1);
        assertEquals(5, bits.bitLength());
        assertThrows(IndexOutOfBoundsException.class, () -> bits.getBit(5));
        assertThrows(IllegalArgumentException.class, () -> bits.appendBits(8, 3));
        assertThrows(NullPointerException.class, () -> bits.appendData(null));
    }

    @Test
    void segmentFactoriesSelectModesAndValidateText() {
        assertEquals(Mode.BYTE, QrSegment.makeBytes(new byte[]{1, 2}).mode);
        assertEquals(Mode.NUMERIC, QrSegment.makeNumeric("12345").mode);
        assertEquals(Mode.ALPHANUMERIC, QrSegment.makeAlphanumeric("HELLO 1").mode);
        assertEquals(Mode.ECI, QrSegment.makeEci(26).mode);
        assertTrue(QrSegment.makeSegments("").isEmpty());
        assertEquals(Mode.NUMERIC, QrSegment.makeSegments("123").get(0).mode);
        assertEquals(Mode.BYTE, QrSegment.makeSegments("中文").get(0).mode);
        assertTrue(QrSegment.isNumeric("0123"));
        assertFalse(QrSegment.isNumeric("12A"));
        assertTrue(QrSegment.isAlphanumeric("A+B"));
        assertFalse(QrSegment.isAlphanumeric("lower"));
        assertThrows(IllegalArgumentException.class, () -> QrSegment.makeNumeric("12A"));
        assertThrows(IllegalArgumentException.class, () -> QrSegment.makeAlphanumeric("lower"));
        assertThrows(IllegalArgumentException.class, () -> QrSegment.makeEci(1_000_000));
    }

    @Test
    void segmentConstructorCopiesDataAndTotalBitsAreCalculated() {
        BitBuffer data = new BitBuffer();
        data.appendBits(3, 2);
        QrSegment segment = new QrSegment(Mode.BYTE, 1, data);
        data.appendBits(0, 1);

        assertEquals(2, segment.getData().bitLength());
        assertEquals(14, QrSegment.getTotalBits(Collections.singletonList(segment), 1));
        assertThrows(IllegalArgumentException.class, () -> new QrSegment(Mode.BYTE, -1, data));
        assertThrows(NullPointerException.class, () -> QrSegment.getTotalBits(Arrays.asList(segment, null), 1));
    }

    @Test
    void advancedSegmentationHandlesUnicodeAndKanji() {
        int[] codePoints = QrSegmentAdvanced.toCodePoints("A😀");
        assertArrayEquals(new int[]{'A', 0x1F600}, codePoints);
        assertEquals(4, QrSegmentAdvanced.countUtf8Bytes(0x1F600));
        assertTrue(QrSegmentAdvanced.isEncodableAsKanji("漢字"));
        assertFalse(QrSegmentAdvanced.isEncodableAsKanji("ABC"));
        assertTrue(QrSegmentAdvanced.isKanji('漢'));
        assertEquals(Mode.KANJI, QrSegmentAdvanced.makeKanji("漢字").mode);
        assertFalse(QrSegmentAdvanced.makeSegmentsOptimally("ABC123中文", Ecc.LOW, 1, 10).isEmpty());
        Mode[] modes = QrSegmentAdvanced.computeCharacterModes(codePoints, 1);
        assertEquals(codePoints.length, modes.length);
        assertFalse(QrSegmentAdvanced.splitIntoSegments(codePoints, modes).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> QrSegmentAdvanced.makeKanji("A"));
        assertThrows(IllegalArgumentException.class,
                () -> QrSegmentAdvanced.makeSegmentsOptimally("x", Ecc.LOW, 0, 1));
    }

    @Test
    void regularEncoderSupportsTextBinarySegmentsMasksAndBounds() {
        QrCode text = QrCode.encodeText("Hello 世界", Ecc.MEDIUM);
        QrCode binary = QrCode.encodeBinary(new byte[]{0, 1, 2}, Ecc.LOW);
        List<QrSegment> segments = QrSegment.makeSegments("HELLO 123");
        QrCode forced = QrCode.encodeSegments(segments, Ecc.HIGH, 1, 10, 3, true);

        assertTrue(text.size >= 21);
        assertTrue(binary.getModule(0, 0));
        assertEquals(3, forced.mask);
        assertFalse(text.getModule(-1, -1));
        assertThrows(IllegalArgumentException.class,
                () -> QrCode.encodeSegments(segments, Ecc.LOW, 0, 1, -1, true));
        assertThrows(DataTooLongException.class,
                () -> QrCode.encodeSegments(Collections.singletonList(QrSegment.makeBytes(new byte[100])),
                        Ecc.HIGH, 1, 1, -1, false));
    }

    @Test
    void regularInternalMathAndMaskHelpersProduceStableValues() {
        assertEquals(208, QrCode.getNumRawDataModules(1));
        assertEquals(19, QrCode.getNumDataCodewords(1, Ecc.LOW));
        byte[] divisor = QrCode.reedSolomonComputeDivisor(7);
        assertEquals(7, divisor.length);
        assertEquals(7, QrCode.reedSolomonComputeRemainder(new byte[]{1, 2, 3}, divisor).length);
        assertThrows(IllegalArgumentException.class, () -> QrCode.reedSolomonComputeDivisor(0));
        assertTrue(QrCode.getBit(2, 1));

        QrCode qr = QrCode.encodeText("MASK", Ecc.LOW);
        assertTrue(qr.getPenaltyScore() >= 0);
        assertTrue(qr.getAlignmentPatternPositions().length >= 0);
        int[] history = new int[7];
        qr.finderPenaltyAddHistory(2, history);
        assertTrue(history[0] >= 2);
        assertTrue(qr.finderPenaltyTerminateAndCount(false, 1, history) >= 0);
        assertTrue(qr.finderPenaltyCountPatterns(history) >= 0);
    }

    @Test
    void renderingCreatesImagesSvgAndPng() throws Exception {
        QrCode qr = QrCode.encodeText("render", Ecc.LOW);
        BufferedImage image = Utils.toImage(qr, 2, 1, 0xFFFFFF, 0x000000);
        assertEquals((qr.size + 2) * 2, image.getWidth());
        assertTrue(Utils.toSvgString(qr, 1, "white", "black").contains("<svg"));
        Path png = tempDir.resolve("qr.png");
        Utils.writePng(image, png.toString());
        assertTrue(java.nio.file.Files.size(png) > 0);
        assertThrows(IllegalArgumentException.class, () -> Utils.toImage(qr, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> Utils.toSvgString(qr, -1, "white", "black"));
        assertEquals(2, Utils.reedSolomonMultiply(1, 2));
    }

    @Test
    void modeAndExceptionExposeExpectedValues() {
        assertEquals(10, Mode.NUMERIC.numCharCountBits(1));
        assertEquals(12, Mode.NUMERIC.numCharCountBits(10));
        assertEquals("too long", new DataTooLongException("too long").getMessage());
    }
}
