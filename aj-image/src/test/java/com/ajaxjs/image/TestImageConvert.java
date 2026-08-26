package com.ajaxjs.image;

import com.ajaxjs.image.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageConvert;
import com.ajaxjs.image.utils.ImageEffect;
import org.junit.jupiter.api.Test;

import static com.ajaxjs.image.TestUtils.getFileAsBytes;

public class TestImageConvert {
    byte[] fileBytes = getFileAsBytes("test.jpg");

    ImageConvert imageConvert = new ImageConvert(fileBytes);

    @Test
    void testToPNG() {
        imageConvert.setImageFormat(ImageFormat.PNG).setQuality(0.8F).outputAsFile("c:/temp/temp.png");
    }

    @Test
    void testToGif() {
        imageConvert.setImageFormat(ImageFormat.GIF).setQuality(0.8F).outputAsFile("c:/temp/temp.gif");
    }

    @Test
    void testToBmp() {
        imageConvert.setImageFormat(ImageFormat.BMP).setQuality(0.8F).outputAsFile("c:/temp/temp.bmp");
    }
}
