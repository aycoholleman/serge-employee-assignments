package org.ayco.serge;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.ayco.serge.Util.*;

public class Main {

  public static void main(String[] args) {
    List<Employee> employees = Employee.loadEmployees();
    Collections.shuffle(employees);
    String[] roles = Util.getRoles(employees);
    System.out.println("Extracted these employee roles from CSV file: " +
          Arrays.stream(roles).collect(Collectors.joining(", ")));
    Location[] locations = Util.createEmptyLocations(employees);
    System.out.println("Extracted these location names from CSV file: " +
          Arrays.stream(locations)
                .map(Location::getName)
                .collect(Collectors.joining(", ")));
    System.out.printf("Distributing %d employees and %d roles across %d locations%n",
          employees.size(),
          roles.length,
          locations.length);
    System.out.println("Assigning employees to locations according to role");
    employees.sort((e1, e2) -> e1.role.compareTo(e2.role));
    for (int i = 0; i < employees.size(); ++i) {
      locations[i % locations.length].addEmployee(employees.get(i));
    }
    optimizeFTESpread(employees, locations, roles);
    minimizeLocationMismatches(locations);
    optimizePreferences(locations);
    writeCsvFile(locations);
  }

  private static void optimizeFTESpread(List<Employee> employees,
        Location[] locations,
        String[] roles) {
    Arrays.stream(locations).forEach(Location::sortByFTE);
    for (String role : roles) {
      System.out.printf("Optimizing FTE distribution for role %s%n", role);
      for (int i = 0; i < countEmployeesInRole(employees, role); ++i) {
        Location loc0 = getLocationWithLowestFTEMean(locations, role);
        if (loc0 == null) continue;
        Location loc1 = getLocationWithHighestFTEMean(locations, role);
        if (loc1 == null) continue;
        int idx0 = loc0.getIndexOfEmpWithLowestFTE(role);
        int idx1 = loc1.getIndexOfEmpWithHighestFTE(role);
        Employee emp0 = loc0.getEmployee(idx0);
        Employee emp1 = loc1.getEmployee(idx1);
        System.out.printf("--> Moving %s from %s to %s%n", emp1.name, loc1, loc0);
        loc0.setEmployee(idx0, emp1);
        System.out.printf("--> Moving %s from %s to %s%n", emp0.name, loc0, loc1);
        loc1.setEmployee(idx1, emp0);
      }
    }
  }

  private static void minimizeLocationMismatches(Location[] locations) {
    MAIN_lOOP:
    for (int i = 0; i < locations.length; ++i) {
      int startIndex = 0;
      Location loc0 = locations[i];
      System.out.printf("Minimizing location mismatches for location %s%n", loc0);
      MID_LOOP:
      while (startIndex < loc0.getEmployees().size()) {
        int idx0 = locations[i].getIndexOfEmpWithLocationMismatch(startIndex);
        if (idx0 == -1) {
          System.out.printf("No location mismatches for employees in %s%n", loc0);
          continue MAIN_lOOP;
        }
        startIndex = idx0 + 1;
        Employee emp0 = loc0.getEmployee(idx0);
        for (int j = 0; j < locations.length; ++j) {
          if (i == j) continue;
          Location loc1 = locations[j];
          int idx1 = loc1.getIndexOfEmpWithLocationMatch(emp0, loc0);
          if (idx1 == -1) continue;
          Employee emp1 = loc1.getEmployee(idx1);
          System.out.printf("--> Moving %s from %s to %s%n", emp1.name, loc1, loc0);
          loc0.setEmployee(idx0, emp1);
          System.out.printf("--> Moving %s from %s to %s%n", emp0.name, loc0, loc1);
          loc1.setEmployee(idx1, emp0);
          continue MID_LOOP;
        }
      }
    }
  }

  private static void optimizePreferences(Location[] locations) {
    System.out.println("Maximizing location preferences");
    MAIN_lOOP:
    for (int i = 0; i < locations.length; ++i) {
      int startIndex = 0;
      while (startIndex < locations[i].getEmployees().size()) {
        int idx0 = locations[i].getIndexOfEmpWithSecondPreference(startIndex);
        if (idx0 == -1) {
          continue MAIN_lOOP;
        }
        startIndex = idx0 + 1;
        Employee emp0 = locations[i].getEmployee(idx0);
        for (int j = 0; j < locations.length; ++j) {
          if (i == j) continue;
          int idx1 = locations[j].getIndexOfEmpWithFirstPreference(emp0, locations[i]);
          if (idx1 == -1) continue;
          Employee emp1 = locations[j].getEmployee(idx1);
          System.out.printf("Moving %s from %s to %s%n",
                emp1.name,
                locations[i],
                locations[j]);
          locations[i].setEmployee(idx0, emp1);
          System.out.printf("--> Moving %s from %s to %s%n",
                emp0.name,
                locations[j],
                locations[i]);
          locations[j].setEmployee(idx1, emp0);
        }
      }
    }
  }


  private static void writeCsvFile(Location[] locations) {
    String path = System.getProperty("user.home")
          + "/employees-per-location."
          + System.currentTimeMillis()
          + ".csv";
    System.out.println("Writing " + path);
    try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
      writer.writeNext(new String[]{
            "Location",
            "Employee",
            "FTE",
            "Preference 1",
            "Preference 2"});
      for (int i = 0; i < locations.length; ++i) {
        for (Employee emp : locations[i].getEmployees()) {
          String[] record = new String[5];
          record[0] = String.valueOf(i + 1);
          record[1] = emp.name;
          record[2] = String.valueOf(emp.fte);
          record[3] = String.valueOf(emp.pref1);
          record[4] = String.valueOf(emp.pref2);
          writer.writeNext(record);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


}
