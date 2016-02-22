package com.sohu.mrd.sonlp.common;


//keyword structure including name,weight and so on
public class Keyword implements Comparable<Keyword> {
    public final String name;
    private double score;
    private double idf;
    private int freq;

    public Keyword(String name, double idf, double weight) {
        //weight作为tf，freq表示它一共出现了多少次
        this.name = name;
        this.idf = idf;
        this.score = idf * weight;
        freq++;
    }

    public void updateWeight(double weight) {
        this.score += weight * idf;
        freq++;
    }

    public int compareTo(Keyword o) {
        if (this.score < o.score) {
            return 1;
        } else {
            return -1;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Keyword) {
            Keyword k = (Keyword) obj;
            return k.name.equals(name);
        } else {
            return false;
        }
    }

    public String toString() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public int getFreq() {
        return freq;
    }
}