package com.swer313.projectstep1.waitinglist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update"
})
class WaitingListRepositoryTest {

    @Autowired
    private WaitingListRepository repository;

    @Test
    void existsActiveAndFindWaitingAndCounts() {
        WaitingListEntry w1 = new WaitingListEntry();
        w1.setRoomTypeId(1L);
        w1.setHotelId(101L);
        w1.setGuestEmail("a@b.com");
        w1.setGuestName("Guest A");
        w1.setCheckIn(LocalDate.now().plusDays(1));
        w1.setCheckOut(LocalDate.now().plusDays(2));
        w1.setStatus(WaitingListStatus.WAITING);
        repository.saveAndFlush(w1);

        boolean exists = repository.existsActiveEntry(1L, "a@b.com", w1.getCheckIn(), w1.getCheckOut());
        assertThat(exists).isTrue();

        List<WaitingListEntry> waiting = repository.findWaitingByRoomTypeAndPeriod(1L, w1.getCheckIn(), w1.getCheckOut());
        assertThat(waiting).isNotEmpty();

        long count = repository.countWaitingByRoomTypeAndPeriod(1L, w1.getCheckIn(), w1.getCheckOut());
        assertThat(count).isGreaterThan(0);

        WaitingListEntry n = new WaitingListEntry();
        n.setRoomTypeId(2L);
        n.setHotelId(102L);
        n.setGuestEmail("n@b.com");
        n.setGuestName("Guest N");
        n.setCheckIn(LocalDate.now().plusDays(3));
        n.setCheckOut(LocalDate.now().plusDays(4));
        n.setStatus(WaitingListStatus.NOTIFIED);
        n.setNotifiedAt(LocalDateTime.now().minusHours(48));
        repository.saveAndFlush(n);

        List<WaitingListEntry> expired = repository.findExpiredNotifications(LocalDateTime.now().minusHours(24));
        assertThat(expired).isNotEmpty();

        WaitingListEntry past = new WaitingListEntry();
        past.setRoomTypeId(3L);
        past.setHotelId(103L);
        past.setGuestEmail("past@b.com");
        past.setGuestName("Past Guest");
        past.setCheckIn(LocalDate.now().minusDays(5));
        past.setCheckOut(LocalDate.now().minusDays(3));
        past.setStatus(WaitingListStatus.WAITING);
        repository.saveAndFlush(past);

        List<WaitingListEntry> dateExpired = repository.findDateExpiredEntries(LocalDate.now());
        assertThat(dateExpired).isNotEmpty();

        var page = repository.findByGuestEmailIgnoreCaseOrderByCreatedAtDesc(
                "a@b.com",
                org.springframework.data.domain.PageRequest.of(0, 10)
        );
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
}