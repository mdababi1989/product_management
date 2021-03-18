package com.mdababi.app;

import com.mdababi.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class Shop {
    static boolean x;
    static boolean y;

    public static void main(String[] args) {
        ProductManager pm = new ProductManager(Locale.US);

        Product p1 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Nice cup of coffee");
        p1 = pm.reviewProduct(p1, Rating.TWO_STAR, "Weak coffee");
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Fine coffee");
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Good coffee");
        p1 = pm.reviewProduct(p1, Rating.FIVE_STAR, "Perfect coffee");
        p1 = pm.reviewProduct(p1, Rating.THREE_STAR, "just add some coffee");

        Product p2 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT_RATED, LocalDate.now().plusDays(2));
        p2 = pm.reviewProduct(p2, Rating.FIVE_STAR, "very nice cake");
        p2 = pm.reviewProduct(p2, Rating.FOUR_STAR, "good cake");
        p2 = pm.reviewProduct(p2, Rating.FIVE_STAR, "perfect cake");

        pm.printProducts((pr1, pr2) -> pr2.getRating().ordinal()-pr1.getRating().ordinal(), p->p.getRating().ordinal()>2);
        pm.printProducts(Comparator.comparing(Product::getPrice).reversed(), p->p.getRating().ordinal()>2);

        System.out.println(pm.getDiscounts());
    }
}
