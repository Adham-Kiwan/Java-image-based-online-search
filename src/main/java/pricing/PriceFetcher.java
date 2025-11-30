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
 * <p>
 * Supported sites (examples): Amazon, eBay, Walmart, Apple Store, iStyle, Adkomsal.
 * Extend or modify selectors as needed for more sites.
 */
public class PriceFetcher {

    /** Timeout duration for WebDriver waits */
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
     * Fetches the first product price from Amazon search results.
     *
     * @param driver      WebDriver instance
     * @param productName Product to search for
     * @return price string or error message
     */
    public String fetchAmazonPrice(WebDriver driver, String productName) {
        try {
            String url = "https://www.amazon.ca/s?k=" + encode(productName)
                    + "&language=en_US&currency=CAD&ref=nb_sb_noss_1";
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.s-main-slot.s-result-list")));
            WebElement priceWhole = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("span.a-price-whole")));
            return priceWhole.getText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Fetches the first product price from Adkomsal search results.
     *
     * @param driver      WebDriver instance
     * @param productName Product to search for
     * @return price string or error message
     */
    public String fetchAdkomsalPrice(WebDriver driver, String productName) {
        try {
            String url = "https://adkomsal.com/?s=" + encode(productName) + "&post_type=product";
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.products li.product")));

            WebElement firstItemLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("ul.products li.product a.woocommerce-LoopProduct-link")));
            String itemUrl = firstItemLink.getAttribute("href");

            driver.get(itemUrl);
            WebElement priceElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("span.woocommerce-Price-amount.amount")));

            return priceElem.getText();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Fetches product price from Apple Store search results.
     *
     * @param driver      WebDriver instance
     * @param productName Product to search for
     * @return price string or error message
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
     * Fetches product price from iStyle Lebanon search results.
     *
     * @param driver      WebDriver instance
     * @param productName Product to search for
     * @return price string or error message
     */
    public String fetchIstylePrice(WebDriver driver, String productName) {
        try {
            String url = "https://istyle.com.lb/search?type=product&q=" + encode(productName);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            WebElement priceElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.fbt_cartCSS.price.price-product-GA-42167675060322.price-product")));
            return priceElem.getAttribute("data-prodprice");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Utility to create a headless ChromeDriver.
     *
     * @return WebDriver instance
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
     * URL-encodes a string for use in query parameters.
     *
     * @param s input string
     * @return encoded string
     */
    private String encode(String s) {
        try {
            s = s.replaceAll("\\s+", " ").trim();
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s.replace(" ", "+");
        }
    }
}