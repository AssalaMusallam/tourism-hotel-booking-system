package com.swer313.projectstep1.catalog.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HotelImageRepositoryTest {

    @Autowired
    private HotelImageRepository repository;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void findByHotelId_returnsImages_and_emptyForUnknown() {
        Hotel h = new Hotel();
        h.setName("H1");
        h.setAddress("Address 1");
        h.setCity("C");
        h.setCountry("CT");
        h.setStatus(Hotel.Status.ACTIVE);
        hotelRepository.saveAndFlush(h);

        HotelImage img = new HotelImage();
        img.setImageUrl("/uploads/img1.png");
        img.setFileName("img1.png");
        img.setHotel(h);
        repository.saveAndFlush(img);

        List<HotelImage> found = repository.findByHotelId(h.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getImageUrl()).isEqualTo("/uploads/img1.png");

        List<HotelImage> none = repository.findByHotelId(9999L);
        assertThat(none).isEmpty();
    }
}