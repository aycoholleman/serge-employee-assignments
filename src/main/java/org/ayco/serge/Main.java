package org.ayco.serge;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static org.ayco.serge.Util.*;

public class Main {

  public static void main(String[] args) {
    List<Employee> employees = Employee.loadEmployees();
    Collections.shuffle(employees);
    String[] roles = Util.getRoles(employees);
    String str = Arrays.stream(roles).collect(joining(", "));
    f("Extracted these employee roles from CSV file: %s%n", str);
    Location[] locations = Util.createEmptyLocations(employees);
    str = Arrays.stream(locations).map(Location::getName).collect(joining(", "));
    f("Extracted these location names from CSV file: %s%n", str);
    f("Distributing %d employees and %d roles across %d locations%n",
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
    System.out.println();
    System.out.println("Optimizations finished");
    DecimalFormat df = new DecimalFormat("0.0");
    f("Global FTE mean: %s%n", df.format(Util.getFTEMean(employees)));
    for (Location loc : locations) {
      f("FTE mean %s: %s%n", loc, df.format(loc.getFTEMean()));
    }
    int pref1Count = Util.countEmployeesInPref1Location(locations);
    int pref2Count = Util.countEmployeesInPref2Location(locations);
    f("Number of employees in location of 1st preference: %d%n", pref1Count);
    f("Number of employees in location of 2nd preference: %d%n", pref2Count);
    double pctSatisfied = (100 * (pref1Count + pref2Count) / employees.size());
    str = df.format(pctSatisfied);
    f("Percentage of employees in location of 1st or 2nd preference: %s%%%n", str);
    writeCsvFile(locations);
  }

  private static void optimizeFTESpread(List<Employee> employees,
        Location[] locations,
        String[] roles) {
    System.out.println("******** Optimizing FTE distribution ********");
    Arrays.stream(locations).forEach(Location::sortByFTE);
    MAIN_LOOP:
    for (String role : roles) {
      f("Optimizing FTE distribution for role %s%n", role);
      int employeeCount = countEmployeesInRole(employees, role);
      if (employeeCount < 3) {
        f("--> Skipped. Only %d employee(s) with this role%n", employeeCount);
        continue;
      }
      Set<Double> lowestCache = HashSet.newHashSet(employeeCount);
      Set<Double> highestCache = HashSet.newHashSet(employeeCount);
      for (int i = 0; i < employeeCount; ++i) {
        Location loc0 = getLocationWithLowestFTEMean(locations, role);
        if (loc0 == null) {
          continue;
        }
        Location loc1 = getLocationWithHighestFTEMean(locations, role);
        if (loc1 == null) {
          continue;
        }
        double lowest = loc0.getFTEMean(role);
        double highest = loc1.getFTEMean(role);
        if (lowestCache.contains(lowest) || highestCache.contains(highest)) {
          continue MAIN_LOOP;
        }
        lowestCache.add(lowest);
        highestCache.add(highest);
        if (lowest / highest > .9) {
          System.out.println("--> FTE mean spread already less than 10%");
          continue MAIN_LOOP;
        }
        int idx0 = loc0.getIndexOfEmpWithLowestFTE(role);
        int idx1 = loc1.getIndexOfEmpWithHighestFTE(role);
        Employee emp0 = loc0.getEmployee(idx0);
        Employee emp1 = loc1.getEmployee(idx1);
        f("--> Moving %s (%s FTE) from %s to %s%n", emp1.name, emp1.fte, loc1, loc0);
        loc0.setEmployee(idx0, emp1);
        f("--> Moving %s (%s FTE) from %s to %s%n", emp0.name, emp0.fte, loc0, loc1);
        loc1.setEmployee(idx1, emp0);
      }
    }
  }

  private static void minimizeLocationMismatches(Location[] locations) {
    System.out.println("******** Minimizing location mismatches ********");
    MAIN_lOOP:
    for (int i = 0; i < locations.length; ++i) {
      Location loc0 = locations[i];
      f("Minimizing location mismatches for location %s%n", loc0);
      int idx0 = loc0.getIndexOfEmpWithLocationMismatch(0);
      if (idx0 == -1) {
        f("--> No location mismatches for employees in %s%n", loc0);
        continue;
      }
      int startIndex = 0;
      MID_LOOP:
      while (startIndex < loc0.getEmployees().size()) {
        idx0 = loc0.getIndexOfEmpWithLocationMismatch(startIndex);
        if (idx0 == -1) {
          continue MAIN_lOOP;
        }
        startIndex = idx0 + 1;
        Employee emp0 = loc0.getEmployee(idx0);
        for (int j = 0; j < locations.length; ++j) {
          if (i == j) continue;
          Location loc1 = locations[j];
          if (emp0.pref1 == loc1.getName() || emp0.pref2 == loc1.getName()) {
            int idx1 = loc1.getIndexOfEmpWithLocationMatch(emp0, loc0);
            if (idx1 == -1) {
              f("--> Unable to solve location mismatch for %s%n", emp0.name);
              continue MID_LOOP;
            }
            Employee emp1 = loc1.getEmployee(idx1);
            f("--> Moving %s from %s to %s%n", emp1.name, loc1, loc0);
            loc0.setEmployee(idx0, emp1);
            f("--> Moving %s from %s to %s%n", emp0.name, loc0, loc1);
            loc1.setEmployee(idx1, emp0);
            continue MID_LOOP;
          }
        }
      }
    }
  }

  private static void optimizePreferences(Location[] locations) {
    System.out.println("******** Optimizing preferences ********");
    int swaps = 0;
    MAIN_lOOP:
    for (int i = 0; i < locations.length; ++i) {
      int startIndex = 0;
      Location loc0 = locations[i];
      while (startIndex < loc0.getEmployees().size()) {
        int idx0 = loc0.getIndexOfEmpWithSecondPreference(startIndex);
        if (idx0 == -1) {
          continue MAIN_lOOP;
        }
        startIndex = idx0 + 1;
        Employee emp0 = loc0.getEmployee(idx0);
        for (int j = 0; j < locations.length; ++j) {
          if (i == j) continue;
          Location loc1 = locations[j];
          int idx1 = loc1.getIndexOfEmpWithFirstPreference(emp0, loc0);
          if (idx1 == -1) continue;
          Employee emp1 = loc1.getEmployee(idx1);
          f("--> Moving %s from %s to %s%n", emp1.name, loc0, loc1);
          loc0.setEmployee(idx0, emp1);
          f("--> Moving %s from %s to %s%n", emp0.name, loc1, loc0);
          loc1.setEmployee(idx1, emp0);
          ++swaps;
        }
      }
    }
    if (swaps == 0) {
      System.out.println("No employees employees were moved to their first preference");
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


  private static void f(String msg, Object... args) {
    System.out.printf(msg, args);
  }


}
