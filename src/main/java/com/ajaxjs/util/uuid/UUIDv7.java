package com.ajaxjs.util.uuid;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public class UUIDv7 {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static UUID randomUUID() {
        byte[] value = randomBytes();
        ByteBuffer buf = ByteBuffer.wrap(value);
        long high = buf.getLong();
        long low = buf.getLong();

        return new UUID(high, low);
    }

    private static byte[] randomBytes() {
        // random bytes
        byte[] value = new byte[16];
        RANDOM.nextBytes(value);

        // current timestamp in ms
        ByteBuffer timestamp = ByteBuffer.allocate(Long.BYTES);
        timestamp.putLong(System.currentTimeMillis());

        // timestamp
        System.arraycopy(timestamp.array(), 2, value, 0, 6);

        // version and variant
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        return value;
    }
}