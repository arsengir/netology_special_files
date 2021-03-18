package com.github.arsengir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        csvToJson();
        xmlToJson();
    }

    private static void xmlToJson() {
        String fileXmlIn = "data.xml";
        String fileJsonOut = "data2.json";
        List<Employee> list = null;
        try {
            list = parseXML(fileXmlIn);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        String json = listToJson(list);
        writeString(fileJsonOut, json);
    }

    private static void csvToJson() {
        String fileCsvIn = "data.csv";
        String fileJsonOut = "data.json";

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> list = parseCSV(columnMapping, fileCsvIn);
        String json = listToJson(list);
        writeString(fileJsonOut, json);
    }

    private static void writeString(String fileName, String text) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(text);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            staff = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return staff;
    }

    private static List<Employee> parseXML(String fileName) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> staff = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(fileName));

        Node root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node employee = nodeList.item(i);
            if (Node.ELEMENT_NODE == employee.getNodeType()) {
                NodeList employeeFields = employee.getChildNodes();
                HashMap<String, String> employeeFieldMap = new HashMap<>();
                for (int j = 0; j < employeeFields.getLength(); j++) {
                    Node employeeField = employeeFields.item(j);
                    if (Node.ELEMENT_NODE == employeeField.getNodeType()) {
                        Element element = (Element) employeeField;
                        employeeFieldMap.put(element.getNodeName(), element.getTextContent());
                    }
                }
                staff.add(new Employee(Long.parseLong(employeeFieldMap.get("id")),
                        employeeFieldMap.get("firstName"),
                        employeeFieldMap.get("lastName"),
                        employeeFieldMap.get("country"),
                        Integer.parseInt(employeeFieldMap.get("age"))));
            }
        }
        return staff;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();

        return gson.toJson(list, listType);
    }

}


