package com.mdababi.data;

import java.math.BigDecimal;
import java.sql.Date;

import static java.text.MessageFormat.format;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductManager {
    private static final String FILENAME = "resources";
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
        return reviewProduct(findProduct(id), rating, comment);
    }

    public Product findProduct(int id) {
        return products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseGet(() -> null);
    }

    public void printProductReport(int id) {
        printProductReport(findProduct(id));
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
       return  products.keySet().stream().collect(Collectors.groupingBy(p -> p.getRating().getStars(), Collectors.collectingAndThen(
                Collectors.summingDouble(product -> product.getDiscount().doubleValue()), discount -> formatter.moneyFormat.format(discount)
        )));
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
