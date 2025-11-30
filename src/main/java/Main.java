import ocr.OCRService;
import ocr.TesseractOCRService;
import ocr.BarcodeScanner;

import java.util.Scanner;

/**
 * Main class for the Product Management application.
 * Provides a menu-driven interface to search, edit, and delete products
 * using OCR and barcode scanning.
 */
public class Main {

    /**
     * Entry point of the application.
     * <p>
     * Displays a menu for the user to:
     * <ul>
     *     <li>Search for a product</li>
     *     <li>Change a product price</li>
     *     <li>Delete products older than a specified date/time</li>
     *     <li>Exit the application</li>
     * </ul>
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.1_1/lib/");
            OCRService ocr = new TesseractOCRService();

            System.out.println("\n----- MENU -----");
            System.out.println("1. Search for a product");
            System.out.println("2. Change product price");
            System.out.println("3. Delete product");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    System.out.print("Enter image file path: ");
                    String path = scanner.nextLine();

                    // First try barcode
                    String barcode = BarcodeScanner.scanBarcode(path);
                    String extractedText = null;

                    if (barcode != null) {
                        System.out.println("\n----- BARCODE DETECTED -----");
                        System.out.println(barcode);
                        extractedText = barcode;
                    } else {
                        System.out.println("No barcode detected. Running OCR...");
                        try {
                            extractedText = ocr.extractText(path);
                            System.out.println("\n----- OCR OUTPUT -----");
                            System.out.println(extractedText);
                        } catch (Exception e) {
                            System.out.println("OCR Error: " + e.getMessage());
                            break;
                        }
                    }

                    try {
                        product.ProductIdentifier.runProductLookup(extractedText);
                    } catch (Exception e) {
                        System.out.println("Error in product lookup: " + e.getMessage());
                    }
                    break;

                case "2":
                    System.out.print("Enter image file path: ");
                    String path2 = scanner.nextLine();

                    // First try barcode
                    String barcode2 = BarcodeScanner.scanBarcode(path2);
                    String extractedText2 = null;

                    if (barcode2 != null) {
                        System.out.println("\n----- BARCODE DETECTED -----");
                        System.out.println(barcode2);
                        extractedText2 = barcode2;
                    } else {
                        System.out.println("No barcode detected. Running OCR...");
                        try {
                            extractedText2 = ocr.extractText(path2);
                            System.out.println("\n----- OCR OUTPUT -----");
                            System.out.println(extractedText2);
                        } catch (Exception e) {
                            System.out.println("OCR Error: " + e.getMessage());
                            break;
                        }
                    }

                    try {
                        product.ProductIdentifier.runProductLookupToEditPrice(extractedText2);
                    } catch (Exception e) {
                        System.out.println("Error in product lookup: " + e.getMessage());
                    }
                    break;

                case "3":
                    try {
                        System.out.print("Enter cutoff date and time (format YYYY-MM-DDTHH:MM:SS): ");
                        String dateTime = scanner.nextLine().trim();
                        String url = "http://localhost:8080/products/older-than/" + dateTime;

                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url(url)
                                .delete()
                                .build();

                        okhttp3.Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            System.out.println("Products deleted successfully.");
                        } else {
                            System.out.println("Failed to delete products. Status code: " + response.code());
                        }
                    } catch (Exception e) {
                        System.out.println("Error deleting products: " + e.getMessage());
                    }
                    break;

                case "0":
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}