package ocr;

/**
 * Interface for OCR services that extract text from images.
 */
public interface OCRService {

    /**
     * Extracts text from an image file.
     *
     * @param imagePath path to the image file
     * @return extracted text as a String
     * @throws Exception if the image cannot be read or OCR fails
     */
    String extractText(String imagePath) throws Exception;
}