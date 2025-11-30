package ocr;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility class for scanning barcodes from image files using ZXing library.
 */
public class BarcodeScanner {

    /**
     * Scans a barcode from the given image file.
     *
     * @param imagePath path to the image containing a barcode
     * @return decoded barcode text, or null if no barcode is found
     */
    public static String scanBarcode(String imagePath) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null; // No barcode found
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}