package com.dimas.testtask.Model;

public class CoinModel {
    public String name;
    public String symbol;
    public double price;

    public CoinModel() {
    }

    public CoinModel(String name, String symbol, double price) {
        this.name = name;
        this.price = price;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
