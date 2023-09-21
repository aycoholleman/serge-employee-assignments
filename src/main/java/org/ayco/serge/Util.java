package org.ayco.serge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

final class Util {

  static String[] getRoles(List<Employee> employees) {
    return employees.stream().map(e -> e.role).collect(toSet()).toArray(String[]::new);
  }

  static Location[] createEmptyLocations(List<Employee> employees) {
    Set<String> prefs = new HashSet<>();
    for (Employee e : employees) {
      prefs.add(e.pref1);
      prefs.add(e.pref2);
    }
    List<Location> locs = new ArrayList<>(prefs.size());
    prefs.forEach(p -> locs.add(new Location(p)));
    return locs.toArray(Location[]::new);
  }

  static int countEmployeesInRole(List<Employee> employees, String role) {
    return (int) employees.stream().filter(e -> e.role.equals(role)).count();
  }

  static double getFTEMean(List<Employee> employees) {
    return employees.stream().mapToDouble(e -> e.fte).average().getAsDouble();
  }

  static double getFTEMean(List<Employee> employees, String role) {
    double sum = 0;
    int cnt = 0;
    for (Employee e : employees) {
      if (e.role.equals(role)) {
        sum += e.fte;
        ++cnt;
      }
    }
    return cnt == 0 ? Double.NaN : sum / cnt;
  }

  static Location getLocationWithLowestFTEMean(Location[] locations, String role) {
    Location loc0 = null;
    for (int i = 0; i < locations.length; ++i) {
      Location loc1 = locations[i];
      if (!loc1.hasEmployeeWithRole(role)) {
        continue;
      }
      if (loc0 == null || (loc1.getFTEMean(role) < loc0.getFTEMean(role))) {
        loc0 = loc1;
      }
    }
    return loc0;
  }

  static Location getLocationWithHighestFTEMean(Location[] locations, String role) {
    Location loc0 = null;
    for (int i = 0; i < locations.length; ++i) {
      Location loc1 = locations[i];
      if (!loc1.hasEmployeeWithRole(role)) {
        continue;
      }
      if (loc0 == null || (loc1.getFTEMean(role) > loc0.getFTEMean(role))) {
        loc0 = loc1;
      }
    }
    return loc0;
  }

  static int countLocationsForRole(Location[] locations, String role) {
    int tot = 0;
    for (Location loc : locations) {
      tot += countEmployeesInRole(loc.getEmployees(), role) == 0 ? 0 : 1;
    }
    return tot;
  }

  static int countEmployeesInPref1Location(Location[] locations) {
    int tot = 0;
    for (Location loc : locations) {
      tot += loc.getEmployees().stream().filter(e -> e.pref1 == loc.getName()).count();
    }
    return tot;
  }

  static int countEmployeesInPref2Location(Location[] locations) {
    int tot = 0;
    for (Location loc : locations) {
      tot += loc.getEmployees().stream().filter(e -> e.pref2 == loc.getName()).count();
    }
    return tot;
  }


}
