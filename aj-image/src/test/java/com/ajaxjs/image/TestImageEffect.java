package com.ajaxjs.image;

import com.ajaxjs.image.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageEffect;
import org.junit.jupiter.api.Test;

import static com.ajaxjs.image.TestUtils.getFileAsBytes;

public class TestImageEffect {
    byte[] fileBytes = getFileAsBytes("test.jpg");

    ImageEffect imageEffect = new ImageEffect(fileBytes);

    @Test
    void testScale() {
        imageEffect.scale(1.4).outputAsFile(ImageFormat.PNG, 0.8f, "c:/temp/temp.png");
    }

    @Test
    void testResize() {
        imageEffect.resize(200, 100, false).
                outputAsFile(ImageFormat.PNG, 0.5f, "c:/temp/temp.png");
    }

    @Test
    void testCreateThumbnail() {
        imageEffect.thumbnail(100, 100, .4).
                outputAsFile(ImageFormat.PNG, 0.5f, "c:/temp/temp.png");
    }

    @Test
    void testRoundCorner() {
        imageEffect.roundCorner(20).outputAsFile(ImageFormat.PNG, 0.5f, "c:/temp/temp.png");
    }
}
