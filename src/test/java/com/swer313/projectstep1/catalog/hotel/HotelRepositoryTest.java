package com.swer313.projectstep1.catalog.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HotelRepositoryTest {

    @Autowired
    private HotelRepository repository;

    @Test
    void findDistinctActiveCities_and_countries_and_autocomplete() {
        Hotel h1 = new Hotel();
        h1.setName("Alpha");
        h1.setAddress("Address 1");
        h1.setCity("C1");
        h1.setCountry("CT1");
        h1.setStatus(Hotel.Status.ACTIVE);
        repository.saveAndFlush(h1);

        Hotel h2 = new Hotel();
        h2.setName("Beta");
        h2.setAddress("Address 2");
        h2.setCity("C2");
        h2.setCountry("CT2");
        h2.setStatus(Hotel.Status.INACTIVE);
        repository.saveAndFlush(h2);

        Hotel h3 = new Hotel();
        h3.setName("Alpha Inn");
        h3.setAddress("Address 3");
        h3.setCity("C1");
        h3.setCountry("CT1");
        h3.setStatus(Hotel.Status.ACTIVE);
        repository.saveAndFlush(h3);

        List<String> cities = repository.findDistinctActiveCities(Hotel.Status.ACTIVE);
        assertThat(cities).containsExactly("C1");

        List<String> countries = repository.findDistinctActiveCountries(Hotel.Status.ACTIVE);
        assertThat(countries).containsExactly("CT1");

        List<String> names = repository.findActiveNameContaining("Alpha", Hotel.Status.ACTIVE, PageRequest.of(0, 10));
        assertThat(names).containsExactly("Alpha", "Alpha Inn");

        List<String> distinctCities = repository.findDistinctCities();
        assertThat(distinctCities).contains("C1", "C2");

        List<String> distinctCountries = repository.findDistinctCountries();
        assertThat(distinctCountries).contains("CT1", "CT2");
    }
}