# 🔍 Product OCR and Price Cache System

> 🚀 Extract product information from images, search online, and cache prices automatically!

This project consists of two Java applications that work together to extract product information from images, search for products online, and cache product prices in a MongoDB database.

---

## 🎯 Quick Start

```bash
# 1. Clone and setup
git clone <repository-url>
cd Java_project

# 2. Configure MongoDB (see Configuration section)
# Edit PriceCacheAPI/src/main/resources/application.properties

# 3. Start Backend API
cd PriceCacheAPI
mvn spring-boot:run

# 4. Run Client (in new terminal)
cd Java-image-based-online-search
mvn compile exec:java -Dexec.mainClass="Main"
```

---

## 📁 Project Structure

```
Java_project/
├── Java-image-based-online-search/    # OCR and product search application
│   ├── src/main/java/
│   │   ├── Main.java                  # Main CLI application
│   │   ├── ocr/                       # OCR services (Tesseract, Barcode scanning)
│   │   ├── product/                   # Product identification and lookup
│   │   └── pricing/                   # Web scraping for prices
│   ├── tessdata/                      # Tesseract language data
│   └── pom.xml
│
└── PriceCacheAPI/                     # Spring Boot REST API for product caching
    ├── src/main/java/
    │   └── com/adham/PriceCacheAPI/
    │       ├── controller/            # REST endpoints
    │       ├── model/                 # Product entity
    │       ├── repository/            # MongoDB repository
    │       └── service/               # Business logic
    ├── src/main/resources/
    │   └── application.properties     # Spring Boot configuration
    └── pom.xml
```

## ✨ Features

### 📸 Java-image-based-online-search
- 🖼️ **OCR Text Extraction**: Uses Tesseract OCR to extract text from product images
- 📊 **Barcode Scanning**: Uses ZXing to scan barcodes from images
- 🔎 **Product Lookup**: Searches for products online and scrapes prices from multiple sources
- 💰 **Price Comparison**: Finds the best price and stores it in the database
- 💻 **Interactive CLI**: Menu-driven interface for product search, price updates, and deletion

### 🌐 PriceCacheAPI
- 🚀 **REST API**: Spring Boot API for managing products
- 🗄️ **MongoDB Integration**: Stores product information with prices and timestamps
- ⚙️ **CRUD Operations**: Create, read, update, and delete products
- 🗑️ **Bulk Operations**: Delete products older than a specified date

## 📋 Prerequisites

### 🛠️ Required Software

1. ☕ **Java 17** or higher
   - Verify installation: `java -version`
   - Download from: https://adoptium.net/

2. 📦 **Maven 3.6+**
   - Verify installation: `maven -version`
   - Download from: https://maven.apache.org/download.cgi

3. 👁️ **Tesseract OCR**
   - 🍎 **macOS**: `brew install tesseract`
   - 🐧 **Linux (Ubuntu/Debian)**: `sudo apt-get install tesseract-ocr`
   - 🪟 **Windows**: Download from https://github.com/UB-Mannheim/tesseract/wiki
   - Verify installation: `tesseract --version`

4. 🍃 **MongoDB Atlas Account** (or local MongoDB instance)
   - Sign up at: https://www.mongodb.com/cloud/atlas
   - Create a cluster and get your connection string

5. 🌐 **Chrome Browser** (for web scraping with Selenium)
   - ChromeDriver is managed automatically by WebDriverManager

## ⚙️ Configuration

### ⚠️ Security Warning

🔒 **IMPORTANT**: The MongoDB connection string contains sensitive credentials. For production use, you should:

1. 🔐 Use environment variables instead of hardcoding credentials
2. 🚫 Never commit credentials to version control
3. 📝 Use a `.gitignore` file to exclude `application.properties` if it contains secrets

### 🔗 Setting Up MongoDB Connection

#### ✅ Option 1: Using Environment Variables (Recommended)

Create a `.env` file in the `PriceCacheAPI` directory (and add it to `.gitignore`):

```properties
MONGODB_URI=mongodb+srv://username:password@cluster-01.pfxlq.mongodb.net/?appName=Cluster-01
MONGODB_DATABASE=products
```

Then update `PriceCacheAPI/src/main/resources/application.properties`:

```properties
spring.application.name=PriceCacheAPI

spring.data.mongodb.uri=${MONGODB_URI:mongodb+srv://your-username:your-password@your-cluster.mongodb.net/?appName=Cluster-01}
spring.data.mongodb.database=${MONGODB_DATABASE:products}

server.port=8080
```

#### ⚡ Option 2: Direct Configuration (Development Only)

Edit `PriceCacheAPI/src/main/resources/application.properties`:

```properties
spring.application.name=PriceCacheAPI

# Replace with your MongoDB Atlas connection string
spring.data.mongodb.uri=mongodb+srv://your-username:your-password@your-cluster.mongodb.net/?appName=Cluster-01
spring.data.mongodb.database=products

server.port=8080
```

#### 📝 MongoDB Atlas Setup Steps

1. ✍️ Sign up at https://www.mongodb.com/cloud/atlas
2. 🆕 Create a new cluster (free tier available)
3. 👤 Create a database user (username and password)
4. 🌍 Whitelist your IP address (or use `0.0.0.0/0` for development)
5. 🔗 Get your connection string from "Connect" → "Connect your application"
6. ✏️ Replace `<password>` and `<username>` in the connection string

### 🔧 Configuring Tesseract Path

If you're on macOS and Tesseract is installed via Homebrew, the path in `Main.java` may need to be updated:

1. Find your Tesseract installation path:
   ```bash
   brew --prefix tesseract
   ```

2. Update the path in `Java-image-based-online-search/src/main/java/Main.java` (line 30):
   ```java
   System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.1_1/lib/");
   ```
   Replace with your actual path from step 1.

3. For Linux/Windows, you may not need this line, or adjust the path accordingly.

## 🚀 Installation and Setup

### 1️⃣ Clone the Repository

```bash
git clone <repository-url>
cd Java_project
```

### 2️⃣ Build the PriceCacheAPI (Backend)

```bash
cd PriceCacheAPI
mvn clean install
```

### 3️⃣ Build the Java-image-based-online-search (Client)

```bash
cd ../Java-image-based-online-search
mvn clean install
```

### 4️⃣ Verify Tesseract Data Files

Ensure the `tessdata` folder exists in `Java-image-based-online-search/` with `eng.traineddata`:

```bash
ls Java-image-based-online-search/tessdata/eng.traineddata
```

If missing, download from: https://github.com/tesseract-ocr/tessdata/blob/main/eng.traineddata

## ▶️ Running the Applications

### 🌐 Start the PriceCacheAPI (Backend)

1. 📂 Navigate to the PriceCacheAPI directory:
   ```bash
   cd PriceCacheAPI
   ```

2. ▶️ Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
   
   Or using the compiled JAR:
   ```bash
   java -jar target/PriceCacheAPI-0.0.1-SNAPSHOT.jar
   ```

3. ✅ The API will start on `http://localhost:8080`

4. 🔍 Verify it's running:
   ```bash
   curl http://localhost:8080/products
   ```

### 💻 Run the Java-image-based-online-search (Client)

1. 📂 In a new terminal, navigate to the client directory:
   ```bash
   cd Java-image-based-online-search
   ```

2. ▶️ Compile and run:
   ```bash
   mvn compile exec:java -Dexec.mainClass="Main"
   ```
   
   Or compile and run manually:
   ```bash
   mvn compile
   java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) Main
   ```

3. 📋 Follow the menu prompts:
   - **1️⃣ Option 1**: 🔍 Search for a product (provide image path)
   - **2️⃣ Option 2**: ✏️ Change product price (provide image path)
   - **3️⃣ Option 3**: 🗑️ Delete products older than a date
   - **0️⃣ Option 0**: 🚪 Exit

## 🔌 API Endpoints

The PriceCacheAPI provides the following REST endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| 📥 GET | `/products` | Get all products |
| 📥 GET | `/products/{id}` | Get product by ID |
| ➕ POST | `/products` | Create a new product |
| ✏️ PUT | `/products/{id}` | Update a product |
| 🗑️ DELETE | `/products/{id}` | Delete a product by ID |
| 🗑️ DELETE | `/products/older-than/{date}` | Delete products older than date (format: `YYYY-MM-DDTHH:MM:SS`) |

### 📖 Example API Usage

```bash
# Get all products
curl http://localhost:8080/products

# Create a product
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Example Product",
    "description": "Product description",
    "price": 29.99,
    "sourceUrl": "https://example.com/product"
  }'

# Delete products older than a date
curl -X DELETE "http://localhost:8080/products/older-than/2024-01-01T00:00:00"
```

## 🔧 Troubleshooting

### ❌ Tesseract Not Found

**🔴 Error**: `UnsatisfiedLinkError` or Tesseract library not found

**✅ Solution**:
- ✔️ Verify Tesseract is installed: `tesseract --version`
- 🔍 Check the `jna.library.path` in `Main.java` matches your installation
- 🍎 On macOS with Homebrew, use: `brew --prefix tesseract` to find the path
- 📁 Ensure `tessdata` folder exists with `eng.traineddata` file

### ❌ MongoDB Connection Failed

**🔴 Error**: `MongoSocketException` or connection timeout

**✅ Solution**:
- ✔️ Verify your MongoDB Atlas cluster is running
- 🌍 Check your IP address is whitelisted in MongoDB Atlas
- 🔍 Verify the connection string format in `application.properties`
- 🔐 Ensure the username and password are correct
- 🌐 Check network connectivity

### ❌ Port 8080 Already in Use

**🔴 Error**: `Port 8080 is already in use`

**✅ Solution**:
- 🛑 Stop other applications using port 8080, or
- ⚙️ Change the port in `application.properties`: `server.port=8081`
- 🔗 Update the API_BASE in `ProductIdentifier.java` if you change the port

### ❌ ChromeDriver Issues

**🔴 Error**: WebDriver or ChromeDriver errors

**✅ Solution**:
- ✔️ Ensure Chrome browser is installed
- 🤖 WebDriverManager should download ChromeDriver automatically
- 📥 If issues persist, manually download ChromeDriver and add to PATH

## 💻 Development

### 📦 Project Dependencies

**📸 Java-image-based-online-search**:
- 👁️ Tesseract OCR (Tess4J 5.4.0)
- 📊 ZXing (Barcode scanning)
- 🕷️ Selenium (Web scraping)
- 🌐 OkHttp (HTTP client)
- 📄 Jackson (JSON processing)
- 🤖 WebDriverManager (ChromeDriver management)

**🌐 PriceCacheAPI**:
- 🚀 Spring Boot 3.5.8
- 🗄️ Spring Data MongoDB
- 🍃 MongoDB Driver 5.6.1
- ⚡ Lombok
- ☕ Java 17

## 📄 License

This project is licensed under the MIT License.  
See the [LICENSE](./LICENSE) file for details.

## 🤝 Author

Adham Kiwan

## 💬 Support

For issues or questions, please [open an issue](<repository-url>/issues) on GitHub.

