package product;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import pricing.PriceFetcher;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles product lookup, price scraping, and database updates.
 * <p>
 * Provides methods to:
 * <ul>
 *     <li>Search for a product in the database</li>
 *     <li>Scrape prices from websites if the product is not found</li>
 *     <li>Save the best price to the database</li>
 *     <li>Compare and update prices if a lower price is found</li>
 * </ul>
 */
public class ProductIdentifier {

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Change this to point to your API base if needed
    private static String API_BASE = "http://localhost:8080";

    /**
     * Looks up a product by its name or barcode, scrapes prices if not found in DB,
     * saves only the best price, and prints the best store price.
     *
     * @param extractedText the product name or barcode extracted from an image
     * @throws Exception if HTTP requests or JSON parsing fails
     */
    public static void runProductLookup(String extractedText) throws Exception {
        extractedText = extractedText.replaceAll("\\s+", " ").trim();
        System.out.println("Searching for product: " + extractedText);

        // ----------- Step 1: check database -----------
        String encodedName = URLEncoder.encode(extractedText, "UTF-8");
        Request getRequest = new Request.Builder()
                .url(API_BASE + "/products?name=" + encodedName)
                .get()
                .build();

        Response getResponse = client.newCall(getRequest).execute();
        String body = getResponse.body().string();

        if (!body.equals("[]") && !body.isEmpty()) {
            System.out.println("\nProduct already in database:");
            System.out.println("Best price: " + getBestPriceFromDB(body));
            return;
        }

        // ----------- Step 2: scrape prices -----------
        System.out.println("Product not found in DB — scraping...");
        PriceFetcher fetcher = new PriceFetcher();
        Map<String, pricing.StoreResult> prices = fetcher.fetchPrices(extractedText);

        System.out.println("\nScraped results:");
        for (Map.Entry<String, pricing.StoreResult> e : prices.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue().price);
        }

        // ----------- Step 3: find BEST store only -----------
        Map.Entry<String, pricing.StoreResult> bestEntry = null;
        double bestValue = Double.MAX_VALUE;

        for (Map.Entry<String, pricing.StoreResult> entry : prices.entrySet()) {
            String priceStr = entry.getValue().price.replaceAll("[^0-9.]", "");
            if (priceStr.isEmpty())
                continue;

            try {
                double value = Double.parseDouble(priceStr);
                if (value < bestValue) {
                    bestValue = value;
                    bestEntry = entry;
                }
            } catch (Exception ignored) {
            }
        }

        if (bestEntry == null) {
            System.out.println("No valid prices found to store.");
            return;
        }

        // ----------- Step 4: Save ONLY the best price ----------
        pricing.StoreResult bestData = bestEntry.getValue();
        String bestStore = bestEntry.getKey();

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", extractedText);
        payload.put("description", "");
        payload.put("price", Double.parseDouble(bestData.price.replaceAll("[^0-9.]", "")));
        payload.put("sourceUrl", bestData.url);

        String json = mapper.writeValueAsString(payload);

        Request post = new Request.Builder()
                .url(API_BASE + "/products")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        client.newCall(post).execute();

        // ----------- Step 5: show best scraped price -----------
        System.out.println("\nSaved BEST PRICE ONLY:");
        System.out.println(bestStore + " — " + bestData.price);
    }

    /**
     * Looks up a product and updates its price in the database if a lower price is found.
     *
     * @param extractedText the product name or barcode extracted from an image
     * @throws Exception if HTTP requests or JSON parsing fails
     */
    public static void runProductLookupToEditPrice(String extractedText) throws Exception {
        extractedText = extractedText.replaceAll("\\s+", " ").trim();
        System.out.println("Searching for product: " + extractedText);

        // ----------- Step 1: check database -----------
        String encodedName = URLEncoder.encode(extractedText, "UTF-8");
        Request getRequest = new Request.Builder()
                .url(API_BASE + "/products?name=" + encodedName)
                .get()
                .build();

        Response getResponse = client.newCall(getRequest).execute();
        String body = getResponse.body().string();

        Double dbPrice = null;
        String recordId = null;

        if (!body.equals("[]") && !body.isEmpty()) {
            // Parse JSON
            java.util.List<java.util.Map<String, Object>> list = mapper.readValue(body, mapper.getTypeFactory()
                    .constructCollectionType(java.util.List.class,
                            mapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class)));

            java.util.Map<String, Object> item = list.get(0);
            dbPrice = Double.parseDouble(item.get("price").toString());
            recordId = item.get("id").toString();

            System.out.println("\nProduct already in database:");
            System.out.println("Current DB price: " + dbPrice);
        }

        // ----------- Step 2: scrape prices -----------
        System.out.println("Scraping websites for updated prices...");
        PriceFetcher fetcher = new PriceFetcher();
        Map<String, pricing.StoreResult> prices = fetcher.fetchPrices(extractedText);

        System.out.println("\nScraped results:");
        for (Map.Entry<String, pricing.StoreResult> e : prices.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue().price);
        }

        // ----------- Step 3: find BEST store only -----------
        Map.Entry<String, pricing.StoreResult> bestEntry = null;
        double bestValue = Double.MAX_VALUE;

        for (Map.Entry<String, pricing.StoreResult> entry : prices.entrySet()) {
            String priceStr = entry.getValue().price.replaceAll("[^0-9.]", "");
            if (priceStr.isEmpty())
                continue;

            try {
                double value = Double.parseDouble(priceStr);
                if (value < bestValue) {
                    bestValue = value;
                    bestEntry = entry;
                }
            } catch (Exception ignored) {
            }
        }

        if (bestEntry == null) {
            System.out.println("No valid prices found to compare.");
            return;
        }

        pricing.StoreResult bestData = bestEntry.getValue();
        String bestStore = bestEntry.getKey();
        double bestPrice = Double.parseDouble(bestData.price.replaceAll("[^0-9.]", ""));

        // ----------- Step 4: compare with DB and update if lower ----------
        if (dbPrice == null) {
            System.out.println("Product not in DB. You may consider adding it first.");
        } else if (bestPrice < dbPrice) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", extractedText);
            payload.put("description", "");
            payload.put("price", bestPrice);
            payload.put("sourceUrl", bestData.url);

            String json = mapper.writeValueAsString(payload);

            Request putRequest = new Request.Builder()
                    .url(API_BASE + "/products/" + recordId)
                    .put(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            client.newCall(putRequest).execute();
            System.out.println("Database updated with new lower price: " + bestPrice + " at " + bestStore);
        } else {
            System.out.println("Existing DB price (" + dbPrice + ") is lower or equal than the best scraped price ("
                    + bestPrice + "). No update performed.");
        }

        System.out.println("\nBest scraped price: " + bestPrice + " at " + bestStore);
    }

    /**
     * Extracts the best price from the database JSON response.
     *
     * @param jsonArray JSON array of products from the database
     * @return String containing the best product identifier and price
     */
    public static String getBestPriceFromDB(String jsonArray) {
        try {
            java.util.List<java.util.Map<String, Object>> list = mapper.readValue(jsonArray, mapper.getTypeFactory()
                    .constructCollectionType(java.util.List.class,
                            mapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class)));

            double best = Double.MAX_VALUE;
            String bestIdentifier = null;

            for (Map<String, Object> item : list) {
                String priceStr = item.get("price").toString().replaceAll("[^0-9.]", "");
                if (!priceStr.isEmpty()) {
                    double value = Double.parseDouble(priceStr);
                    if (value < best) {
                        best = value;
                        // Use title or sourceUrl as identifier
                        bestIdentifier = item.get("title") != null ? item.get("title").toString()
                                : item.get("sourceUrl") != null ? item.get("sourceUrl").toString() : "unknown";
                    }
                }
            }

            return bestIdentifier + " — " + best;
        } catch (Exception e) {
            return "Error parsing DB best price";
        }
    }

    /**
     * Extracts the best price from a map of scraped store results.
     *
     * @param prices Map of store names to StoreResult objects
     * @return String containing the store name and its best price
     */
    public static String getBestPriceFromResults(Map<String, pricing.StoreResult> prices) {
        double best = Double.MAX_VALUE;
        String bestStore = null;

        for (Map.Entry<String, pricing.StoreResult> entry : prices.entrySet()) {
            String priceStr = entry.getValue().price.replaceAll("[^0-9.]", "");
            if (!priceStr.isEmpty()) {
                try {
                    double value = Double.parseDouble(priceStr);
                    if (value < best) {
                        bestStore = entry.getKey();
                        best = value;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return bestStore + " — " + best;
    }
}