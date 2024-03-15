package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;

import java.util.Collection;
import java.util.Set;

/**
 * Data Access Object for breeds.
 * Implements access functionality to the application's persistent data store regarding breeds.
 */
public interface BreedDao {

  /**
   * Gets all breeds from the persistent data store.
   *
   * @return all breeds.
   */
  Collection<Breed> allBreeds();

  /**
   * Gets all breeds that match an element inside the set of provided {@code breedIds} from the persistent data store.
   *
   * @param breedIds A set of IDs to get
   * @return a collection of breeds
   */
  Collection<Breed> findBreedsById(Set<Long> breedIds);

  /**
   * Get the breeds that match the given search parameters.
   * Parameters that are {@code null} are ignored.
   * The name is considered a match, if the given parameter is a substring of the field in breed.
   *
   * @param searchParams the parameters to use in searching.
   * @return the breeds where all given parameters match.
   */
  Collection<Breed> search(BreedSearchDto searchParams);
}