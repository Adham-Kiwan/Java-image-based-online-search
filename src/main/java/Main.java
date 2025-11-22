import ocr.OCRService;
import ocr.TesseractOCRService;
import ocr.BarcodeScanner;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter image file path:");
            String path = scanner.nextLine();

            // Set path to the folder containing libtesseract.dylib
            System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.1_1/lib/");

            // First try barcode
            String barcode = BarcodeScanner.scanBarcode(path);
            if (barcode != null) {
                System.out.println("\n----- BARCODE DETECTED -----");
                System.out.println(barcode);
            } else {
                System.out.println("No barcode detected. Running OCR...");

                OCRService ocr = new TesseractOCRService();
                try {
                    String text = ocr.extractText(path);
                    System.out.println("\n----- OCR OUTPUT -----");
                    System.out.println(text);
                } catch (Exception e) {
                    System.out.println("OCR Error: " + e.getMessage());
                }
            }
        }
    }
}