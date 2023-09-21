package org.ayco.serge;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

final class Employee {

  static List<Employee> loadEmployees() {
    String path = System.getProperty("user.home") + "/employees.csv";
    System.out.println("Reading " + path);
    try (CSVReader reader = new CSVReader(new FileReader(path))) {
      reader.skip(1);
      List<String[]> records = reader.readAll();
      List<Employee> emps = new ArrayList<>(records.size());
      System.out.println("Number of records in CSV file: " + records.size());
      for (int i = 0; i < records.size(); ++i) {
        String[] record = records.get(i);
        if (record.length == 1 && record[0].strip().isEmpty()) {
          continue; // empty line
        } else if (record.length != 6) {
          System.out.printf("Error at line %d. Invalid number of fields: %d%n",
                i + 2,
                record.length);
        }
        Employee emp = new Employee(
              record[0],
              record[1],
              record[2],
              parseInt(record, i, 3),
              parseInt(record, i, 4),
              record[5]);
        emps.add(emp);
      }
      return emps;
    } catch (FileNotFoundException e0) {
      throw new RuntimeException("Missing file: " + path);
    } catch (IOException e1) {
      throw new RuntimeException(e1.getMessage());
    } catch (CsvException e2) {
      throw new RuntimeException(e2.getMessage());
    }
  }

  private static int parseInt(String[] rec, int recNo, int fieldNo) {
    try {
      return new BigInteger(rec[fieldNo]).intValueExact();
    } catch (Exception e) {
      String err = String.format("Error at line %d, column %d. Invalid integer: %s",
            recNo + 2,
            fieldNo + 1,
            rec[fieldNo]);
      throw new RuntimeException(err);
    }
  }

  private static double parseDouble(String[] rec, int recNo, int fieldNo) {
    try {
      return Double.valueOf(rec[fieldNo]);
    } catch (Exception e) {
      String err = String.format("Error at line %d, column %d. Invalid number: %s",
            recNo + 2,
            fieldNo + 1,
            rec[fieldNo]);
      throw new RuntimeException(err);
    }
  }

  final String name;
  final String pref1;
  final String pref2;
  final int age;
  final double fte;
  final String role;

  Employee(String name, String pref1, String pref2, int age, double fte, String role) {
    this.name = name.intern();
    this.pref1 = pref1.intern();
    this.pref2 = pref2.intern();
    this.age = age;
    this.fte = fte;
    this.role = role.intern();
  }

  public String toString() {
    return new StringBuilder(50)
          .append("{name=")
          .append(name)
          .append(";role=")
          .append(role)
          .append(";pref1=")
          .append(pref1)
          .append(";pref2=")
          .append(pref2)
          .append(";age=")
          .append(age)
          .append(";fte=")
          .append(fte)
          .append("}")
          .toString();
  }
}
