package ocr;

import net.sourceforge.tess4j.Tesseract;
import java.io.File;

public class TesseractOCRService implements OCRService {

    @Override
    public String extractText(String imagePath) throws Exception {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // folder containing eng.traineddata
        tesseract.setLanguage("eng");

        return tesseract.doOCR(new File(imagePath));
    }
}
