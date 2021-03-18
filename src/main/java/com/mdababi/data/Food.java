package com.mdababi.data;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Food extends Product {
    private LocalDate bestBefore;

   Food(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        super(id, name, price, rating);
        this.bestBefore = bestBefore;
    }

    @Override
    public BigDecimal getDiscount() {
        return LocalDate.now().isEqual(bestBefore) ? super.getDiscount(): BigDecimal.ZERO;
    }

    @Override
    public Product applyRating(Rating newRating) {
        return new Food(this.getId(), getName(), getPrice(), newRating , bestBefore);
    }

    @Override
    public String toString() {
        return "Food{" + super.toString() +
                ", bestBefore=" + bestBefore +
                '}';
    }
}
