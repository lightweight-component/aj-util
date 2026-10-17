package com.ajaxjs.image;

import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageEncoder;
import com.ajaxjs.image.utils.ImageTypeHandler;
import org.junit.jupiter.api.Test;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用可观测的 ImageIO 测试提供器，验证异常路径确实释放资源。
 */
class TestImageResourceLifecycle {
    @Test
    void encoderClosesCacheAndDisposesWriterOnEveryPath() throws Exception {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        List<ImageWriterSpi> existing = new ArrayList<>();
        registry.getServiceProviders(ImageWriterSpi.class, true).forEachRemaining(existing::add);
        ProbeWriterSpi spi = new ProbeWriterSpi();
        registry.registerServiceProvider(spi);

        try {
            for (ImageWriterSpi other : existing)
                registry.setOrdering(ImageWriterSpi.class, spi, other);
            for (int stage = 0; stage < 3; stage++) {
                spi.stage = stage;
                spi.disposed = false;
                spi.stream = null;
                CallerStream caller = new CallerStream();
                BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
                if (stage == 0)
                    ImageEncoder.write(image, ImageFormat.PNG, 0.8f, caller);
                else if (stage == 1) {
                    UncheckedIOException failure = assertThrows(UncheckedIOException.class,
                            () -> ImageEncoder.write(image, ImageFormat.PNG, 0.8f, caller));
                    assertEquals("injected write failure", failure.getCause().getMessage());
                } else
                    assertThrows(IllegalStateException.class,
                            () -> ImageEncoder.write(image, ImageFormat.PNG, 0.8f, caller));
                assertTrue(spi.disposed, "编码器在正常、写入失败和参数设置失败时都需要释放");
                assertNotNull(spi.stream);
                assertThrows(IOException.class, () -> spi.stream.getStreamPosition());
                assertFalse(caller.closed, "内部缓存流关闭不能关闭调用方的流");
                caller.write(42);
            }
        } finally {
            registry.deregisterServiceProvider(spi);
        }
    }

    @Test
    void readerReleasesResourcesOnSuccessReadFailureAndParameterFailure() throws Exception {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        ProbeReaderSpi spi = new ProbeReaderSpi();
        registry.registerServiceProvider(spi);
        try {
            for (int stage = 0; stage < 3; stage++) {
                spi.disposed = false;
                spi.failRead = stage == 1;
                ImageTypeHandler handler = new ImageTypeHandler(new byte[]{1}, "aj-image-probe");
                if (stage == 0)
                    assertEquals(2, handler.read((ImageReadParam) null).getWidth());
                else if (stage == 1) {
                    UncheckedIOException failure = assertThrows(UncheckedIOException.class,
                            () -> handler.read((ImageReadParam) null));
                    assertEquals("injected read failure", failure.getCause().getMessage());
                } else
                    assertThrows(IllegalArgumentException.class,
                            () -> handler.read(new Rectangle(0, 0, 0, 0), null));
                assertTrue(spi.disposed);
                assertThrows(IOException.class, () -> spi.stream.getStreamPosition());
                assertThrows(IllegalStateException.class, () -> handler.read((ImageReadParam) null));
                handler.close(); // 关闭操作可以重复执行。
            }
        } finally {
            registry.deregisterServiceProvider(spi);
        }
    }

    @Test
    void headerFailureAndMetadataOnlyUseAlsoReleaseResources() throws Exception {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        ProbeReaderSpi spi = new ProbeReaderSpi();
        registry.registerServiceProvider(spi);
        try {
            spi.failHeader = true;
            UncheckedIOException failure = assertThrows(UncheckedIOException.class,
                    () -> new ImageTypeHandler(new byte[]{1}, "aj-image-probe"));
            assertEquals("injected header failure", failure.getCause().getMessage());
            assertTrue(spi.disposed);
            assertThrows(IOException.class, () -> spi.stream.getStreamPosition());

            spi.failHeader = false;
            spi.disposed = false;
            try (ImageTypeHandler handler = new ImageTypeHandler(new byte[]{1}, "aj-image-probe")) {
                assertEquals(2, handler.getImageInfo().getWidth().intValue());
                assertFalse(spi.disposed);
            }
            assertTrue(spi.disposed);
            assertThrows(IOException.class, () -> spi.stream.getStreamPosition());
        } finally {
            registry.deregisterServiceProvider(spi);
        }
    }

    private static class CallerStream extends ByteArrayOutputStream {
        boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }

    private static class ProbeWriterSpi extends ImageWriterSpi {
        int stage;
        boolean disposed;
        ImageOutputStream stream;

        ProbeWriterSpi() {
            super("aj-image-test", "1", new String[]{"png"}, null, null,
                    ProbeWriter.class.getName(), new Class<?>[]{ImageOutputStream.class}, null,
                    false, null, null, null, null, false, null, null, null, null);
        }

        @Override
        public boolean canEncodeImage(ImageTypeSpecifier type) {
            return true;
        }

        @Override
        public ImageWriter createWriterInstance(Object extension) {
            return new ProbeWriter(this);
        }

        @Override
        public String getDescription(Locale locale) {
            return "resource test writer";
        }
    }

    private static class ProbeWriter extends ImageWriter {
        final ProbeWriterSpi spi;

        ProbeWriter(ProbeWriterSpi spi) {
            super(spi);
            this.spi = spi;
        }

        @Override
        public void setOutput(Object output) {
            super.setOutput(output);
            spi.stream = (ImageOutputStream) output;
        }

        @Override
        public ImageWriteParam getDefaultWriteParam() {
            if (spi.stage == 2) throw new IllegalStateException("injected parameter failure");
            return super.getDefaultWriteParam();
        }

        @Override
        public void write(IIOMetadata metadata, IIOImage image, ImageWriteParam param) throws IOException {
            if (spi.stage == 1) throw new IOException("injected write failure");
            spi.stream.writeByte(1);
        }

        @Override
        public void dispose() {
            spi.disposed = true;
        }

        @Override
        public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
            return null;
        }

        @Override
        public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier type, ImageWriteParam param) {
            return null;
        }

        @Override
        public IIOMetadata convertStreamMetadata(IIOMetadata metadata, ImageWriteParam param) {
            return null;
        }

        @Override
        public IIOMetadata convertImageMetadata(IIOMetadata metadata, ImageTypeSpecifier type, ImageWriteParam param) {
            return null;
        }
    }

    private static class ProbeReaderSpi extends ImageReaderSpi {
        boolean failHeader;
        boolean failRead;
        boolean disposed;
        ImageInputStream stream;

        ProbeReaderSpi() {
            super("aj-image-test", "1", new String[]{"aj-image-probe"}, null, null,
                    ProbeReader.class.getName(), new Class<?>[]{ImageInputStream.class}, null,
                    false, null, null, null, null, false, null, null, null, null);
        }

        @Override
        public boolean canDecodeInput(Object source) {
            return true;
        }

        @Override
        public ImageReader createReaderInstance(Object extension) {
            return new ProbeReader(this);
        }

        @Override
        public String getDescription(Locale locale) {
            return "resource test reader";
        }
    }

    private static class ProbeReader extends ImageReader {
        final ProbeReaderSpi spi;

        ProbeReader(ProbeReaderSpi spi) {
            super(spi);
            this.spi = spi;
        }

        @Override
        public void setInput(Object input, boolean forward, boolean ignoreMetadata) {
            super.setInput(input, forward, ignoreMetadata);
            spi.stream = (ImageInputStream) input;
        }

        @Override
        public int getNumImages(boolean search) {
            return 1;
        }

        @Override
        public int getWidth(int index) throws IOException {
            if (spi.failHeader) throw new IOException("injected header failure");
            return 2;
        }

        @Override
        public int getHeight(int index) {
            return 2;
        }

        @Override
        public Iterator<ImageTypeSpecifier> getImageTypes(int index) {
            return Collections.singleton(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)).iterator();
        }

        @Override
        public IIOMetadata getStreamMetadata() {
            return null;
        }

        @Override
        public IIOMetadata getImageMetadata(int index) {
            return null;
        }

        @Override
        public BufferedImage read(int index, ImageReadParam param) throws IOException {
            if (spi.failRead) throw new IOException("injected read failure");
            return new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        }

        @Override
        public void dispose() {
            spi.disposed = true;
        }
    }
}
