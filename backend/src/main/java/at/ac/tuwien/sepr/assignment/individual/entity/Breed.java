package at.ac.tuwien.sepr.assignment.individual.entity;

public class Breed {
  private long id;
  private String name;

  public long getId() {
    return id;
  }

  public Breed setId(long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Breed setName(String name) {
    this.name = name;
    return this;
  }
}
