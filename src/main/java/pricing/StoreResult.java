package pricing;

/**
 * Represents the result of a price scraping from a store.
 * Contains the price as a string and the URL of the product page.
 */
public class StoreResult {
    
    /** Price of the product (as a string, may include currency symbols) */
    public String price;

    /** URL of the product page in the store */
    public String url;

    /**
     * Constructs a new StoreResult.
     *
     * @param price the price of the product
     * @param url   the URL of the product page
     */
    public StoreResult(String price, String url) {
        this.price = price;
        this.url = url;
    }
}