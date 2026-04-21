package com.swer313.projectstep1.catalog.amenities;

import com.swer313.projectstep1.catalog.hotel.Hotel;
import com.swer313.projectstep1.catalog.hotel.HotelRepository;
import com.swer313.projectstep1.catalog.room.RoomType;
import com.swer313.projectstep1.catalog.room.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AmenitySpecificationsTest {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    @DisplayName("nameEq_filters_case_insensitively_with_trimmed_input")
    void nameEq_filters_case_insensitively_with_trimmed_input() {
        amenityRepository.deleteAll();

        Amenity one = new Amenity();
        one.setName("WiFi");
        one.setDescription("desc long enough");
        one.setCategory(Amenity.AmenityCategory.CONNECTIVITY);
        amenityRepository.save(one);

        List<Amenity> found = amenityRepository.findAll(AmenitySpecifications.nameEq("  wifi  "));

        assertEquals(1, found.size());
        assertEquals("WiFi", found.get(0).getName());
    }

    @Test
    @DisplayName("categoryEq_filters_by_category")
    void categoryEq_filters_by_category() {
        amenityRepository.deleteAll();

        Amenity a1 = new Amenity("Xxx", "descdescdesc", Amenity.AmenityCategory.CLEANING);
        Amenity a2 = new Amenity("Yyy", "descdescdesc", Amenity.AmenityCategory.PARKING);
        amenityRepository.saveAll(List.of(a1, a2));

        List<Amenity> found = amenityRepository.findAll(
                AmenitySpecifications.categoryEq(Amenity.AmenityCategory.CLEANING)
        );

        assertEquals(1, found.size());
        assertEquals(Amenity.AmenityCategory.CLEANING, found.get(0).getCategory());
    }

    @Test
    @DisplayName("premiumEq_filters_by_premium_flag")
    void premiumEq_filters_by_premium_flag() {
        amenityRepository.deleteAll();

        Amenity p = new Amenity("Premium", "descdescdesc", Amenity.AmenityCategory.COMFORT);
        p.setPremium(true);

        Amenity np = new Amenity("Normal", "descdescdesc", Amenity.AmenityCategory.COMFORT);
        np.setPremium(false);

        amenityRepository.saveAll(List.of(p, np));

        List<Amenity> found = amenityRepository.findAll(AmenitySpecifications.premiumEq(true));

        assertEquals(1, found.size());
        assertTrue(found.get(0).isPremium());
    }

    @Test
    @DisplayName("activeEq_filters_by_active_flag")
    void activeEq_filters_by_active_flag() {
        amenityRepository.deleteAll();

        Amenity a = new Amenity("Act", "descdescdesc", Amenity.AmenityCategory.OUTDOOR);
        a.setActive(true);

        Amenity b = new Amenity("Ina", "descdescdesc", Amenity.AmenityCategory.OUTDOOR);
        b.setActive(false);

        amenityRepository.saveAll(List.of(a, b));

        List<Amenity> found = amenityRepository.findAll(AmenitySpecifications.activeEq(true));

        assertTrue(found.stream().allMatch(Amenity::isActive));
    }

    @Test
    @DisplayName("qLike_matches_name_or_description_case_insensitively")
    void qLike_matches_name_or_description_case_insensitively() {
        amenityRepository.deleteAll();

        Amenity a = new Amenity("Pool", "Indoor Swimming Pool", Amenity.AmenityCategory.WELLNESS);
        Amenity b = new Amenity("Gym", "24/7 fitness area", Amenity.AmenityCategory.WELLNESS);
        amenityRepository.saveAll(List.of(a, b));

        List<Amenity> found = amenityRepository.findAll(AmenitySpecifications.qLike("pool"));

        assertEquals(1, found.size());
        assertEquals("Pool", found.get(0).getName());
    }

    @Test
    @DisplayName("createdAtBetween_from_only_filters_lower_bound")
    void createdAtBetween_from_only_filters_lower_bound() {
        amenityRepository.deleteAll();

        Amenity a = new Amenity("FromX", "descdescdesc", Amenity.AmenityCategory.COMFORT);
        Amenity saved = amenityRepository.save(a);

        LocalDateTime created = saved.getCreatedAt();

        List<Amenity> found = amenityRepository.findAll(
                AmenitySpecifications.createdAtBetween(created.minusSeconds(1), null)
        );

        assertTrue(found.stream().anyMatch(x -> x.getId().equals(saved.getId())));
    }

    @Test
    @DisplayName("createdAtBetween_to_only_filters_upper_bound")
    void createdAtBetween_to_only_filters_upper_bound() {
        amenityRepository.deleteAll();

        Amenity a = new Amenity("Tooo", "descdescdesc", Amenity.AmenityCategory.COMFORT);
        Amenity saved = amenityRepository.save(a);

        LocalDateTime created = saved.getCreatedAt();

        List<Amenity> found = amenityRepository.findAll(
                AmenitySpecifications.createdAtBetween(null, created.plusSeconds(1))
        );

        assertTrue(found.stream().anyMatch(x -> x.getId().equals(saved.getId())));
    }

    @Test
    @DisplayName("createdAtBetween_between_filters_range")
    void createdAtBetween_between_filters_range() {
        amenityRepository.deleteAll();

        Amenity a = new Amenity("Betw", "descdescdesc", Amenity.AmenityCategory.COMFORT);
        Amenity saved = amenityRepository.save(a);

        LocalDateTime created = saved.getCreatedAt();

        List<Amenity> found = amenityRepository.findAll(
                AmenitySpecifications.createdAtBetween(created.minusSeconds(1), created.plusSeconds(1))
        );

        assertTrue(found.stream().anyMatch(x -> x.getId().equals(saved.getId())));
    }


}