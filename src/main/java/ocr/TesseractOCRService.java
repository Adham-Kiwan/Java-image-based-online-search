package ocr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;

/**
 * Implementation of OCRService using Tesseract OCR engine.
 * Provides methods to extract text from images with preprocessing steps.
 */
public class TesseractOCRService implements OCRService {

    /**
     * Extracts text from an image file using Tesseract OCR.
     *
     * @param imagePath path to the image file
     * @return extracted text from the image
     * @throws Exception if the image cannot be read or OCR fails
     */
    @Override
    public String extractText(String imagePath) throws Exception {
        BufferedImage input = ImageIO.read(new File(imagePath));
        if (input == null) throw new IOException("Could not read image file: " + imagePath);

        BufferedImage preprocessed = preprocessForOCR(input);

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // folder containing eng.traineddata
        tesseract.setLanguage("eng");
        tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);

        return tesseract.doOCR(preprocessed);
    }

    /**
     * Preprocesses the image to enhance OCR accuracy.
     *
     * @param src input image
     * @return preprocessed image
     */
    private BufferedImage preprocessForOCR(BufferedImage src) {
        BufferedImage gray = toGrayscale(src);
        int minDim = Math.min(gray.getWidth(), gray.getHeight());
        if (minDim < 300) gray = scaleImage(gray, 2.0);
        BufferedImage denoised = medianBlur(gray);
        BufferedImage contrasted = contrastStretch(denoised);
        BufferedImage binary = adaptiveThreshold(contrasted);
        BufferedImage sharp = sharpenImage(binary);
        return sharp;
    }

    /**
     * Converts an image to grayscale.
     *
     * @param src input image
     * @return grayscale image
     */
    private BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    /**
     * Scales an image by a given factor.
     *
     * @param src   input image
     * @param scale scaling factor
     * @return scaled image
     */
    private BufferedImage scaleImage(BufferedImage src, double scale) {
        int w = (int) Math.round(src.getWidth() * scale);
        int h = (int) Math.round(src.getHeight() * scale);
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    /**
     * Applies a simple median-like blur to reduce noise.
     *
     * @param src input image
     * @return denoised image
     */
    private BufferedImage medianBlur(BufferedImage src) {
        float[] kernel = {
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f
        };
        Kernel k = new Kernel(3, 3, kernel);
        ConvolveOp op = new ConvolveOp(k);
        return op.filter(src, null);
    }

    /**
     * Enhances contrast of an image.
     *
     * @param src input image
     * @return contrast-stretched image
     */
    private BufferedImage contrastStretch(BufferedImage src) {
        Raster r = src.getRaster();
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        r.getPixels(0, 0, w, h, pixels);

        int min = 255, max = 0;
        for (int p : pixels) {
            if (p < min) min = p;
            if (p > max) max = p;
        }
        if (max == min) return src;

        float scale = 255f / (max - min);
        for (int i = 0; i < pixels.length; i++) {
            int v = Math.round((pixels[i] - min) * scale);
            pixels[i] = Math.min(255, Math.max(0, v));
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        out.getRaster().setPixels(0, 0, w, h, pixels);
        return out;
    }

    /**
     * Applies adaptive thresholding to convert grayscale to binary image.
     *
     * @param src input grayscale image
     * @return binary image
     */
    private BufferedImage adaptiveThreshold(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Raster raster = src.getRaster();
        int[] pixels = new int[w * h];
        raster.getPixels(0, 0, w, h, pixels);

        int sum = 0;
        for (int v : pixels) sum += v;
        int mean = sum / pixels.length;

        BufferedImage bin = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        WritableRaster wr = bin.getRaster();
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = (pixels[idx++] > mean) ? 255 : 0;
                wr.setSample(x, y, 0, val);
            }
        }
        return bin;
    }

    /**
     * Sharpens the image to improve OCR accuracy.
     *
     * @param src input image
     * @return sharpened image
     */
    private BufferedImage sharpenImage(BufferedImage src) {
        float[] sharpKernel = {
                0.f, -1.f, 0.f,
                -1.f, 5.f, -1.f,
                0.f, -1.f, 0.f
        };
        Kernel kernel = new Kernel(3, 3, sharpKernel);
        ConvolveOp op = new ConvolveOp(kernel);
        return op.filter(src, null);
    }
}