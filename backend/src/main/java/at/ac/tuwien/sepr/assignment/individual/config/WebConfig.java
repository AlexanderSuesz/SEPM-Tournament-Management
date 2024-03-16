package at.ac.tuwien.sepr.assignment.individual.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for web-related settings.
 * This configuration effectively disables Cross-Origin Resource Sharing (CORS) for non-production profiles.
 * Disabling CORS is helpful during development but is not recommended for production environments.
 */
//TODO: Should disable when done developing?
// this configuration effectively disables CORS; this is helpful during development but a bad idea in production
@Profile("!prod")
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Configures Cross-Origin Resource Sharing (CORS) mappings.
   * This method allows all origins and permits various HTTP methods for requests.
   *
   * @param registry the CorsRegistry to configure CORS mappings
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "POST", "OPTIONS", "HEAD", "DELETE", "PUT", "PATCH");
  }
}
