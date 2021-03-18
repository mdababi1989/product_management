package com.mdababi.data;

import lombok.Getter;

@Getter
public class Review implements Comparable<Review>{
    private Rating rating;
    private String comment;

    public Review(Rating rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public int compareTo(Review other) {
        return other.getRating().ordinal()- this.getRating().ordinal();
    }
}
