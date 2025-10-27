package com.ajaxjs.util.io;


import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Slf4j
public class CmdHelper {
    public static void exec(String cmd, Function<String, Boolean> callback) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (ObjectHelper.hasText(line) && !callback.apply(line))
                        break;
                }
            }
            process.waitFor();
        } catch (IOException e) {
            log.warn("Executed command failed:" + cmd, e);
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.warn("Executed command failed:" + cmd, e);
            throw new RuntimeException(e);
        }

    }
}
