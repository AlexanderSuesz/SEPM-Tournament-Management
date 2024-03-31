package at.ac.tuwien.sepr.assignment.individual.entity;

import java.time.LocalDate;

/**
 * Represents a tournament in the persistent data store.
 */
public class Tournament {
  private long id;
  private String name;
  private LocalDate startDate;
  private LocalDate endDate;

  public long getId() {
    return id;
  }

  public Tournament setId(long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Tournament setName(String name) {
    this.name = name;
    return this;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public Tournament setStartDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Tournament setEndDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  @Override
  public String toString() {
    return "Tournament{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", startDate=" + startDate
        + ", endDate=" + endDate
        + '}';
  }
}
