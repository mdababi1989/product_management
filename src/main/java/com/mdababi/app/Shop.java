package com.mdababi.app;

import com.mdababi.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class Shop {

    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();

        Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        //pm.printProductReport(101);

        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Nice cup of Tea");
        p1 = pm.reviewProduct(101, Rating.TWO_STAR, "Weak Tea");
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Fine Tea");
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Good Tea");
        p1 = pm.reviewProduct(101, Rating.FIVE_STAR, "Perfect Tea");
        p1 = pm.reviewProduct(101, Rating.THREE_STAR, "just add some Tea");
        pm.printProductReport(101, "en-GB", "");

        /*Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        p2 = pm.reviewProduct(p2, Rating.ONE_STAR, "Weak coffee");
        p2 = pm.reviewProduct(p2, Rating.FIVE_STAR, "Perfect coffee");
        p2 = pm.reviewProduct(p2, Rating.THREE_STAR, "just add some coffee");
        pm.printProductReport(102, "en-GB");

        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT_RATED, LocalDate.now().plusDays(2));
        p3 = pm.reviewProduct(p3, Rating.FIVE_STAR, "very nice cake");
        p3 = pm.reviewProduct(p3, Rating.FOUR_STAR, "good cake");
        p3 = pm.reviewProduct(p3, Rating.FIVE_STAR, "perfect cake");
        pm.printProductReport(103, "en-GB");

        Product p4 = pm.createProduct(104, "Cookie", BigDecimal.valueOf(2.99), Rating.NOT_RATED, LocalDate.now());
        p4 = pm.reviewProduct(p4, Rating.THREE_STAR, "very nice Cookie");
        p4 = pm.reviewProduct(p4, Rating.THREE_STAR, "nice Cookie");

        Product p5 = pm.createProduct(105, "Hot Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED);
        p5 = pm.reviewProduct(p5, Rating.FOUR_STAR, "very nice Chocolate");
        p5 = pm.reviewProduct(p5, Rating.FOUR_STAR, "very nice Chocolate");

        Product p6 = pm.createProduct(106, "Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED, LocalDate.now().plusDays(3));
        p6 = pm.reviewProduct(p6, Rating.TWO_STAR, "too sweet");
        p6 = pm.reviewProduct(p6, Rating.THREE_STAR, "very nice Chocolate");
        p6 = pm.reviewProduct(p6, Rating.TWO_STAR, "very nice Chocolate");
        p6 = pm.reviewProduct(p6, Rating.ONE_STAR, "very nice Chocolate");


        pm.printProductReport(103, "en-GB");*/

        pm.printProducts((pr1, pr2) -> pr2.getRating().ordinal() - pr1.getRating().ordinal(), p -> p.getRating().ordinal() > 2, "en-GB");
        pm.printProducts(Comparator.comparing(Product::getPrice).reversed(), p -> p.getRating().ordinal() > 2, "en-GB");

        System.out.println(pm.getDiscounts("en-GB"));


    }
}
