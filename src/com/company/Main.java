package com.company;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException {
        DataGenerator dataGen = new DataGenerator();
        dataGen.generateTable("table.csv");
    }
}
