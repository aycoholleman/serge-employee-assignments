package org.ayco.serge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Location {

  private final List<Employee> employees = new ArrayList<>(20);

  private final String name;

  Location(String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }

  List<Employee> getEmployees() {
    return employees;
  }

  double getFTEMean() {
    return Util.getFTEMean(employees);
  }

  double getFTEMean(String role) {
    return Util.getFTEMean(employees, role);
  }

  void sortByFTE() {
    Collections.sort(employees, (e1, e2) -> Double.compare(e1.fte, e2.fte));
  }

  boolean isEmpty() {
    return employees.isEmpty();
  }

  Employee getEmployee(int index) {
    return employees.get(index);
  }

  void addEmployee(Employee emp) {
    employees.add(emp);
  }

  void setEmployee(int index, Employee emp) {
    employees.set(index, emp);
  }

  int countEmployeesInRole(String role) {
    return Util.countEmployeesInRole(employees, role);
  }

  boolean hasEmployeeWithRole(String role) {
    return countEmployeesInRole(role) != 0;
  }

  int getIndexOfEmpWithLowestFTE(String role) {
    int idx = -1;
    Employee emp = null;
    for (int i = 0; i < employees.size(); ++i) {
      Employee e = employees.get(i);
      if (e.role.equals(role)) {
        if (emp == null || e.fte < emp.fte) {
          emp = e;
          idx = i;
        }
      }
    }
    return idx;
  }

  int getIndexOfEmpWithHighestFTE(String role) {
    int idx = -1;
    Employee emp = null;
    for (int i = 0; i < employees.size(); ++i) {
      Employee e = employees.get(i);
      if (e.role.equals(role)) {
        if (emp == null || e.fte > emp.fte) {
          emp = e;
          idx = i;
        }
      }
    }
    return idx;
  }

  int getIndexOfEmpWithLocationMismatch(int startIndex) {
    for (int i = startIndex; i < employees.size(); ++i) {
      Employee e = employees.get(i);
      if (e.pref1 != this.name && e.pref2 != this.name) {
        return i;
      }
    }
    return -1;
  }

  int getIndexOfEmpWithLocationMatch(Employee emp, Location loc) {
    for (int i = 0; i < employees.size(); ++i) {
      Employee e = employees.get(i);
      if (e.fte == emp.fte
            && e.role.equals(emp.role)
            && (e.pref1.equals(loc.name) || e.pref2.equals(loc.name))) {
        return i;
      }
    }
    return -1;
  }

  int getIndexOfEmpWithSecondPreference(int startIndex) {
    for (int i = startIndex; i < employees.size(); ++i) {
      Employee e = employees.get(i);
      if (e.pref2.equals(this.name)) {
        return i;
      }
    }
    return -1;
  }


  int getIndexOfEmpWithFirstPreference(Employee emp, Location loc) {
    for (int i = 0; i < employees.size(); ++i) {
      Employee e = employees.get(i);
      if (e.fte == emp.fte
            && e.role.equals(emp.role)
            && e.pref1.equals(loc.name)
            && emp.pref1.equals(this.name)) {
        return i;
      }
    }
    return -1;
  }


  public String toString() {
    return name;
  }

}
