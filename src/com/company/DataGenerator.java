package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataGenerator {
    private List<String> namesM;
    private List<String> namesF;
    private List<String> middleNamesM;
    private List<String> middleNamesF;
    private List<String> surnamesM;
    private List<String> surnamesF;
    private List<String> streets;
    private List<List<String>> city;
    private List<List<String>> country;
    private List<List<String>> region;
    private Random r;
    private Boolean gender;

    DataGenerator() throws IOException{
        r = new Random();
        loadDatabases();
    }

    private List<String> readFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/com/company/"+filename));

        List<String> tokens = new ArrayList<>();
        for (String line : lines) {
            Collections.addAll(tokens, line.split(";"));
        }
        return tokens;
    }

    private List<List<String>> readCsv(String filename) throws IOException {
        List<String> columns;
        List<List<String>> values;
        File inputF = new File("src/com/company/"+filename);
        InputStream inputFS = new FileInputStream(inputF);
        try (BufferedReader br =  new BufferedReader(new InputStreamReader(inputFS))) {
            String firstLine = br.readLine();
            if (firstLine == null) throw new IOException("empty file");
            columns = Arrays.asList(firstLine.split(","));
            values = br.lines()
                    .map(line -> Arrays.asList(line.split(",")))
                    .collect(Collectors.toList());
        }
        return values;
    }

    private void loadDatabases() throws IOException {
        namesF = readFile("NameFeminine.txt");
        namesM = readFile("NameMusculine.txt");
        surnamesF = readFile("SurnameFeminine.txt");
        surnamesM = readFile("SurnameMusculine.txt");
        middleNamesF = readFile("MiddleNameFeminine.txt");
        middleNamesM = readFile("MiddleNameMusculine.txt");
        city = readCsv("city.csv");
        region = readCsv("region.csv");
        country = readCsv("country.csv");
        streets = readFile("streets.txt");
    }

    private boolean randomBoolean(){
        return Math.random() < 0.5;
    }

    private String generateName() throws IOException {
        String Name = new String();
        gender = randomBoolean();
        if (gender){
            Name += surnamesM.get(r.nextInt(surnamesM.size())) + ',';
            Name += namesM.get(r.nextInt(namesM.size()))+ ',';
            Name += middleNamesM.get(r.nextInt(middleNamesM.size()))+ ',';
        }
        else {
            Name += surnamesF.get(r.nextInt(surnamesF.size())) + ',';
            Name += namesF.get(r.nextInt(namesF.size())) + ',';
            Name += middleNamesF.get(r.nextInt(middleNamesF.size())) + ',';
        }
        return Name;
    }

    private GregorianCalendar generateDateOfBirth() {
        GregorianCalendar gc = new GregorianCalendar();
        int year = 1900+r.nextInt(111); // [1900, 2010]
        gc.set(gc.YEAR, year);
        int dayOfYear = 1 + r.nextInt(gc.getActualMaximum(gc.DAY_OF_YEAR));
        gc.set(gc.DAY_OF_YEAR, dayOfYear);
        return gc;
    }

    private int calculateAge(GregorianCalendar gc) {
        Calendar now = Calendar.getInstance();
        int age = now.get(now.YEAR) - gc.get(gc.YEAR) - 1;
        if ((now.get(now.MONTH) > gc.get(gc.MONTH))||((now.get(now.MONTH) == gc.get(gc.MONTH)) && (now.get(now.DAY_OF_MONTH) >= gc.get(gc.DAY_OF_MONTH))))
        {
            age += 1;
        }
        return age;
    }

    private List<String> generateTown(){
        List<String> generatedTown = new ArrayList<String>() ;
        List<String> cityParameters = city.get(r.nextInt(city.size()));
        generatedTown.add(cityParameters.get(3));
        for (List<String> line : region) {
            if (Integer.parseInt(line.get(0)) == Integer.parseInt(cityParameters.get(2))) {
                generatedTown.add(line.get(3));
                break;
            }
        }
        for (List<String> line : country) {
            if (Integer.parseInt(line.get(0)) == Integer.parseInt(cityParameters.get(1))) {
                generatedTown.add(line.get(2));
                break;
            }
        }
        return generatedTown;
    }

    private String generateZipcode(){
        // current version of code supports only Russia, Belarus, and Ukraine
        // zipcode in Ukraine contains 5 digits
        // since in the task it was explicitly mentioned that zipcode must contain 6 digits, we ignore this fact
        int zipcode = r.nextInt(1000000);
        String text = String.format("%06d", zipcode);
        return text;
    }

    private String generateAddress(){
        String addr = streets.get(r.nextInt(streets.size())) + ",";
        Integer buildingNumber = 1+r.nextInt(100);
        Integer roomNumber = 1+r.nextInt(100);
        return addr + buildingNumber.toString() + ',' + roomNumber.toString();
    }

    private String generatePerson() throws IOException{
        String person = generateName();
        GregorianCalendar gc = generateDateOfBirth();
        Integer age = calculateAge(gc);
        person += age.toString() + ',';
        if (gender){
            person += "М,";
        } else {
            person += "Ж,";
        }
        person+= gc.get(gc.DAY_OF_MONTH) + "-" + (gc.get(gc.MONTH) + 1) + "-" + gc.get(gc.YEAR) + ',';
        List<String> cityOfBirth = generateTown();
        person += cityOfBirth.get(0) + ',';
        person += generateZipcode() + ',';
        List<String> currentCity = generateTown();
        person += currentCity.get(2) + ',' + currentCity.get(1) + ',' + currentCity.get(0)+',';
        person += generateAddress() + System.getProperty("line.separator");
        return person;
    }

    public void generateTable (String filename)  throws IOException {
        Path path = Paths.get("src/com/company/"+filename);

        try (BufferedWriter writer = Files.newBufferedWriter(path))
        {
            writer.write("Фамилия,Имя,Отчество,Возраст,Пол,Дата Рождения, Место Рождения, Почтовый Индекс, Страна Проживания,Город,Область,Улица,Дом,Квартира"+ System.getProperty("line.separator") );
            int numberOfPersons = r.nextInt(30)+1;
            IntStream.range(0, numberOfPersons).forEach(i->{
                try {
                    writer.write(generatePerson());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
        System.out.println("Файл создан. Путь: "+path.toString());
    }


}
