package pricing;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * PriceFetcher uses Selenium WebDriver to fetch product prices from multiple
 * e-commerce websites.
 * Supported sites (examples): Amazon, eBay, Walmart.
 * Extend or modify selectors as needed for more sites.
 */
public class PriceFetcher {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    /**
     * Fetches prices from multiple sites for the given product name.
     * 
     * @param productName the name of the product to search for
     * @return map of website name to StoreResult (price string and url)
     */
    public Map<String, StoreResult> fetchPrices(String productName) {
        Map<String, StoreResult> prices = new HashMap<>();
        WebDriver driver = getHeadlessDriver();
        try {
            prices.put("Amazon", new StoreResult(fetchAmazonPrice(driver, productName), driver.getCurrentUrl()));
            prices.put("Adkomsal", new StoreResult(fetchAdkomsalPrice(driver, productName), driver.getCurrentUrl()));
            prices.put("Apple Store", new StoreResult(fetchAppleStorePrice(driver, productName), driver.getCurrentUrl()));
            prices.put("iStyle", new StoreResult(fetchIstylePrice(driver, productName), driver.getCurrentUrl()));
        } finally {
            driver.quit();
        }
        return prices;
    }

    /**
     * Fetch the first product price from Amazon search results.
     */
    public String fetchAmazonPrice(WebDriver driver, String productName) {
        try {
            String url = "https://www.amazon.ca/s?k=" + encode(productName)
                    + "&language=en_US&currency=CAD&ref=nb_sb_noss_1";
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            // Wait for search results
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.s-main-slot.s-result-list")));
            // Find the first price-whole element in results
            WebElement priceWhole = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("span.a-price-whole")));
            return priceWhole.getText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Fetch the first product price from Adkomsal search results.
     */
    public String fetchAdkomsalPrice(WebDriver driver, String productName) {
        try {
            String url = "https://adkomsal.com/?s=" + encode(productName) + "&post_type=product";
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            // Wait for a product container to be present
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("ul.products li.product")));

            // Click first product link
            WebElement firstItemLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("ul.products li.product a.woocommerce-LoopProduct-link")));
            String itemUrl = firstItemLink.getAttribute("href");

            driver.get(itemUrl);

            // Fetch the price element
            WebElement priceElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("span.woocommerce-Price-amount.amount")));

            return priceElem.getText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Fetch price from Apple Store search results.
     */
    public String fetchAppleStorePrice(WebDriver driver, String productName) {
        try {
            String url = "https://www.apple.com/us/search/" + encode(productName) + "?src=globalnav";
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            WebElement priceElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("span.rf-producttile-pricecurrent")));

            return priceElem.getText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Fetch price from iStyle Lebanon search results.
     */
    public String fetchIstylePrice(WebDriver driver, String productName) {
        try {
            String url = "https://istyle.com.lb/search?type=product&q=" + encode(productName);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            WebElement priceElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.fbt_cartCSS.price.price-product-GA-42167675060322.price-product")));
            String price = priceElem.getAttribute("data-prodprice");
            return price;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Utility to create a headless ChromeDriver.
     */
    private WebDriver getHeadlessDriver() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        System.setProperty("webdriver.manager.disable", "true");
        System.setProperty("webdriver.http.factory", "jdk-http-client"); // suppress CDP warnings

        ChromeOptions options = new ChromeOptions();
        options.setBinary("/Applications/Brave Browser.app/Contents/MacOS/Brave Browser");

        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1200,800");

        return new ChromeDriver(options);
    }

    /**
     * URL-encode a string for use in query parameters.
     */
    private String encode(String s) {
        try {
            // Remove newlines and extra spaces
            s = s.replaceAll("\\s+", " ").trim();
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s.replace(" ", "+");
        }
    }
}
