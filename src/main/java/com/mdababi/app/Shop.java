package com.mdababi.app;

import com.mdababi.data.Product;
import com.mdababi.data.ProductManager;
import com.mdababi.data.Rating;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Shop {
    private static AtomicInteger clientCount = new AtomicInteger(0);


    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();
        createData(pm);

        Callable<String> client = () -> {
            String clientId = "Client " + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(6) + 101;
            String languageTag = ProductManager.getSupportedLocales().stream().skip(ThreadLocalRandom.current().nextInt(4)).findFirst().get();
            StringBuilder log = new StringBuilder();
            log.append(clientId + " " + threadName + "\n-\tstart of log\t-\n");
            log.append(pm.getDiscounts(languageTag).entrySet().stream().map(entry -> entry.getKey() + "\t" + entry.getValue()).collect(Collectors.joining("\n")));
            Product product = pm.reviewProduct(productId, Rating.FOUR_STAR, "Yet another review");
            log.append((product != null) ? "\nProduct " + productId + " reviewed\n" : "\nProduct " + productId + " not reviewed\n");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId+" generated report for "+productId+" product");
            log.append("\n-\tend of log\t-\n");
            return log.toString();
        };

        List<Callable<String>> clients = Stream.generate(()->client).limit(5).collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            results.stream().forEach(result->{
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retreiving client log "+e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients "+e.getMessage());
        }
        executorService.shutdown();


    }

    private static void createData(ProductManager pm) {
        Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Nice cup of Tea");
        p1 = pm.reviewProduct(101, Rating.TWO_STAR, "Weak Tea");
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Fine Tea");
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Good Tea");
        p1 = pm.reviewProduct(101, Rating.FIVE_STAR, "Perfect Tea");
        p1 = pm.reviewProduct(101, Rating.THREE_STAR, "just add some Tea");

        Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        p2 = pm.reviewProduct(102, Rating.ONE_STAR, "Weak coffee");
        p2 = pm.reviewProduct(102, Rating.FIVE_STAR, "Perfect coffee");
        p2 = pm.reviewProduct(102, Rating.THREE_STAR, "just add some coffee");

        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT_RATED, LocalDate.now().plusDays(2));
        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "very nice cake");
        p3 = pm.reviewProduct(103, Rating.FOUR_STAR, "good cake");
        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "perfect cake");

        Product p4 = pm.createProduct(104, "Cookie", BigDecimal.valueOf(2.99), Rating.NOT_RATED, LocalDate.now());
        p4 = pm.reviewProduct(104, Rating.THREE_STAR, "very nice Cookie");
        p4 = pm.reviewProduct(104, Rating.THREE_STAR, "nice Cookie");

        Product p5 = pm.createProduct(105, "Hot Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED);
        p5 = pm.reviewProduct(105, Rating.FOUR_STAR, "very nice Chocolate");
        p5 = pm.reviewProduct(105, Rating.FOUR_STAR, "very nice Chocolate");

        Product p6 = pm.createProduct(106, "Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED, LocalDate.now().plusDays(3));
        p6 = pm.reviewProduct(106, Rating.TWO_STAR, "too sweet");
        p6 = pm.reviewProduct(106, Rating.THREE_STAR, "very nice Chocolate");
        p6 = pm.reviewProduct(106, Rating.TWO_STAR, "very nice Chocolate");
        p6 = pm.reviewProduct(106, Rating.ONE_STAR, "very nice Chocolate");
    }

}
