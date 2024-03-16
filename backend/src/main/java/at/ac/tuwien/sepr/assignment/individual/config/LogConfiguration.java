package at.ac.tuwien.sepr.assignment.individual.config;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Configuration class for logging settings.
 * This configuration registers a {@link LogFilter} to log every HTTP request and response.
 */
@Configuration
public class LogConfiguration {

  /**
   * Registers a {@link LogFilter} as a filter bean to log every HTTP request and response.
   * The filter is applied to all URL patterns.
   *
   * @return a {@link FilterRegistrationBean} for the {@link LogFilter}
   */
  @Bean
  public FilterRegistrationBean<OncePerRequestFilter> logFilter() {
    var reg = new FilterRegistrationBean<OncePerRequestFilter>(new LogFilter());
    reg.addUrlPatterns("/*");
    reg.setName("logFilter");
    reg.setOrder(Ordered.LOWEST_PRECEDENCE);
    return reg;
  }
}
