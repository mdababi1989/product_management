package com.mdababi.data;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

@Getter
public abstract class Product implements Rateable<Product>{
    public final static BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);

    private int id;
    private String name;
    private BigDecimal price;
    private Rating rating;

    Product(int id, String name, BigDecimal price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    public BigDecimal getDiscount() {
        return price.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public abstract Product applyRating(Rating newRating);

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                ", discount=" + getDiscount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Product)) return false;
        Product product = (Product) o;
        return this.id == product.id ;
    }

    @Override
    public int hashCode() {
        int hash =5;
        hash = 25*hash +this.id;
        return hash;
    }

    public LocalDate getBestBefore(){
        return LocalDate.now();
    }
}
