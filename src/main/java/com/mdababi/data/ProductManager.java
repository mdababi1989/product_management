package com.mdababi.data;

import com.mdababi.exceptions.ProductManagerException;

import java.io.*;
import java.math.BigDecimal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private static final String FILENAME = "resources";
    private final ResourceBundle config = ResourceBundle.getBundle("config");
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private Map<Product, List<Review>> products = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();
    //private ResourceFormatter formatter;
    private final static Map<String, ResourceFormatter> formatters =
            Map.of(
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA)
            );
    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));

    private static final ProductManager productManager = new ProductManager();

    private ProductManager() { }

    public static ProductManager getInstance(){
        return productManager;
    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;
        try{
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception e) {
            logger.log(Level.INFO, "Error adding product "+e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = null;
        try{
            writeLock.lock();
            product = new Drink(id, name, price, rating);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception e) {
            logger.log(Level.INFO, "Error adding product "+e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }
        return product;
    }

    private Product reviewProduct(Product product, Rating rating, String comment) {
        List<Review> reviews = products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comment));
        double average = reviews.stream().mapToInt(r -> r.getRating().ordinal()).average().orElse(0);
        product = product.applyRating(Rateable.convert((int) Math.round(average)));
        Collections.sort(reviews);
        products.put(product, reviews);
        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comment) {
        try {
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comment);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }finally {
            writeLock.unlock();
        }
    }

    public Product findProduct(int id) throws ProductManagerException {
        try {
            readLock.lock();
            return products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseThrow(
                    () -> new ProductManagerException("Product with id " + id + " not found"));
        }finally {
            readLock.unlock();
        }
    }

    public void printProductReport(int id, String languageTag, String client) {
        try {
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error printing product " + e.getMessage());
        }finally {
            readLock.unlock();
        }
    }

    private void printProductReport(Product product, String languageTag, String client) throws IOException {
        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        Files.createDirectories(reportsFolder);
        Path productFile = reportsFolder.resolve(MessageFormat.format(config.getString("report.file"), product.getId(), client));
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE), StandardCharsets.UTF_8))) {
            out.append(formatter.formatProduct(product) + System.lineSeparator());
            if (reviews.size() == 0) {
                out.append(formatter.getText("no.reviews") + System.lineSeparator());
            } else {
                out.append(reviews.stream().map(r -> formatter.formatReview(r) + System.lineSeparator()).collect(Collectors.joining()));
            }
        }
    }

    public void printProducts(Comparator<Product> sorter, Predicate<Product> filter, String languageTag) {
        try{
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            String txt = products.keySet().stream().sorted(sorter).filter(filter).map(p -> formatter.formatProduct(p) + "\n").collect(Collectors.joining());
            System.out.printf(txt);
        }finally {
            readLock.unlock();
        }

    }

    public Map<String, String> getDiscounts(String languageTag) {
        try{
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            return products.keySet().stream().collect(Collectors.groupingBy(p -> p.getRating().getStars(), Collectors.collectingAndThen(
                    Collectors.summingDouble(product -> product.getDiscount().doubleValue()), discount -> formatter.moneyFormat.format(discount)
            )));
        }finally {
            readLock.unlock();
        }

    }

    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String) values[0]), Rateable.convert(Integer.parseInt((String) values[1])), (String) values[2]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing review " + text + " " + e.getMessage());
        }
        return review;
    }

    private List<Review> loadReviews(Product product) {
        List<Review> reviews = new ArrayList<>();
        Path file = dataFolder.resolve(Path.of(MessageFormat.format(config.getString("reviews.data.file"), product.getId())));
        if (Files.exists(file)) {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8")).map(text -> parseReview(text)).filter(review -> review != null).collect(Collectors.toList());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error loading reviews "+e.getMessage());
            }
        }
        return reviews;
    }

    private Product loadProduct(Path file) {
        Product product = null;
        if (Files.exists(file)) {
            try {
                product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8")).findFirst().orElseThrow());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error loading product "+e.getMessage());
            }
        }
        return product;
    }

    private void loadAllData(){
        try {
            products = Files.list(dataFolder).filter(file->file.getFileName().toString().startsWith("product")).map(file->loadProduct(file))
                    .filter(product -> product!=null).collect(Collectors.toMap(product -> product, product -> loadReviews(product)));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading data "+e.getMessage());
        }
    }



    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[0]) {
                case "D":
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
                    break;
            }


        } catch (ParseException | NumberFormatException | DateTimeException e) {
            logger.log(Level.WARNING, "Error parsing product " + text + " " + e.getMessage());
        }
        return product;
    }

    private static class ResourceFormatter {
        private Locale locale;
        private ResourceBundle resource;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale) {
            this.locale = locale;
            resource = ResourceBundle.getBundle(FILENAME, locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct(Product product) {
            return MessageFormat.format(
                    resource.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore())
            );
        }

        private String formatReview(Review review) {
            return MessageFormat.format(
                    resource.getString("review"),
                    review.getRating(),
                    review.getComment()
            );
        }

        private String getText(String key) {
            return resource.getString(key);
        }
    }

}
