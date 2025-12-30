package com.apex.core.dto;

/**
 * 评分分布
 */
public class RatingDistribution {
    private int rating1;
    private int rating2;
    private int rating3;
    private int rating4;
    private int rating5;

    public int getRating1() {
        return rating1;
    }

    public void setRating1(int rating1) {
        this.rating1 = rating1;
    }

    public int getRating2() {
        return rating2;
    }

    public void setRating2(int rating2) {
        this.rating2 = rating2;
    }

    public int getRating3() {
        return rating3;
    }

    public void setRating3(int rating3) {
        this.rating3 = rating3;
    }

    public int getRating4() {
        return rating4;
    }

    public void setRating4(int rating4) {
        this.rating4 = rating4;
    }

    public int getRating5() {
        return rating5;
    }

    public void setRating5(int rating5) {
        this.rating5 = rating5;
    }

    public int getTotal() {
        return rating1 + rating2 + rating3 + rating4 + rating5;
    }

    @Override
    public String toString() {
        return String.format("1星:%d, 2星:%d, 3星:%d, 4星:%d, 5星:%d (总计:%d)",
                rating1, rating2, rating3, rating4, rating5, getTotal());
    }
}