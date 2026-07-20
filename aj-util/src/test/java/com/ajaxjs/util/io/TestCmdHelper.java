package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class TestCmdHelper {
    @Test
    void drainsStderrAndStdoutAfterCallbackStops() {
        String java = System.getProperty("java.home") + "/bin/java";
        String command = java + " -cp " + System.getProperty("java.class.path") + " " + NoisyProcess.class.getName();
        AtomicInteger callbacks = new AtomicInteger();

        assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> CmdHelper.exec(command, line -> callbacks.incrementAndGet() < 1));
        assertEquals(1, callbacks.get());
    }

    public static class NoisyProcess {
        public static void main(String[] args) {
            for (int i = 0; i < 20_000; i++)
                System.err.println("error-" + i);

            System.out.println("first");
            System.out.println("second");
        }
    }
}
