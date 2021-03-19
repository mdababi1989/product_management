package com.mdababi.data;

import com.mdababi.exceptions.ProductManagerException;

import java.math.BigDecimal;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private static final String FILENAME = "resources";
    private ResourceBundle config = ResourceBundle.getBundle("config");
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private Map<Product, List<Review>> products = new HashMap<>();
    private ResourceFormatter formatter;
    private static Map<String, ResourceFormatter> formatters =
            Map.of(
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA)
            );

    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String toLanguageTag) {
        changeLocale(toLanguageTag);
    }

    public void changeLocale(String languageTag) {
        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product reviewProduct(Product product, Rating rating, String comment) {
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
            return reviewProduct(findProduct(id), rating, comment);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }
    }

    public Product findProduct(int id) throws ProductManagerException {
        return products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseThrow(
                () -> new ProductManagerException("Product with id " + id + " not found"));
    }

    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void printProductReport(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatter.formatProduct(product));
        sb.append("\n");
        List<Review> reviews = products.get(product);
        if (reviews.size() == 0) {
            sb.append(formatter.getText("no.reviews") + "\n");
        } else {
            reviews.stream().map(r -> formatter.formatReview(r) + "\n").collect(Collectors.joining());
        }
        System.out.println(sb.toString());
    }

    public void printProducts(Comparator<Product> sorter, Predicate<Product> filter) {
        String txt = products.keySet().stream().sorted(sorter).filter(filter).map(p -> formatter.formatProduct(p) + "\n").collect(Collectors.joining());
        System.out.printf(txt);
    }

    public Map<String, String> getDiscounts() {
        return products.keySet().stream().collect(Collectors.groupingBy(p -> p.getRating().getStars(), Collectors.collectingAndThen(
                Collectors.summingDouble(product -> product.getDiscount().doubleValue()), discount -> formatter.moneyFormat.format(discount)
        )));
    }

    public void parseReview(String text) {
        try {
            Object[] values = reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String) values[0]), Rateable.convert(Integer.parseInt((String) values[1])), (String) values[2]);
        } catch (ParseException|NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing review " + text+" " +e.getMessage());
        }
    }

    public void parseProduct(String text) {
        try {
            Object[] values = productFormat.parse(text);
            int id= Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[0]){
                case "D":
                    createProduct( id,  name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    createProduct( id,  name, price, rating, bestBefore);
                    break;
            }


        } catch (ParseException|NumberFormatException| DateTimeException e) {
            logger.log(Level.WARNING, "Error parsing product " + text+" " +e.getMessage());
        }
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
