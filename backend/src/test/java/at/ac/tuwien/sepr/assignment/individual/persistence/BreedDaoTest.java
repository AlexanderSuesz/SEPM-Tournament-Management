package at.ac.tuwien.sepr.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "datagen"})
@SpringBootTest
public class BreedDaoTest extends TestBase {
  @Autowired
  BreedDao dao;

  @Test
  public void searchForOneExistingBreedSuccessfullyFindsBreed() {
    var foundBreeds = dao.findBreedsById(Set.of(-1L));
    assertNotNull(foundBreeds);
    // This assert is a bit verbose. Just matching the containing elements is enough
    assertAll(
        () -> assertThat(foundBreeds).isNotEmpty(),
        () -> assertThat(foundBreeds.size()).isEqualTo(1),
        () -> assertThat(foundBreeds)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder((new Breed()).setId(-1).setName("Andalusian"))
    );
  }


  @Test
  public void searchForThreeExistingBreedSuccessfullyFindsThreeBreeds() {
    var foundBreeds = dao.findBreedsById(Set.of(-1L, -3L, -11L));
    assertNotNull(foundBreeds);
    assertThat(foundBreeds)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                    (new Breed()).setId(-1).setName("Andalusian"),
                    (new Breed()).setId(-3).setName("Arabian"),
                    (new Breed()).setId(-11).setName("Lipizzaner"));
  }

  @Test
  public void searchForOneExistingAndOneNonexistingBreedSuccessfullyFindsExistingBreed() {
    var foundBreeds = dao.findBreedsById(Set.of(-1L, -99999L));
    assertNotNull(foundBreeds);
    assertThat(foundBreeds)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder((new Breed()).setId(-1).setName("Andalusian"));
  }

  @Test
  public void searchForOneNonexistingBreedSuccessfullyFindsNothing() {
    var foundBreeds = dao.findBreedsById(Set.of(-99999L));
    assertNotNull(foundBreeds);
    assertThat(foundBreeds).isEmpty();
  }
}
