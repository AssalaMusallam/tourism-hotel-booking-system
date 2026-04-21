package com.swer313.projectstep1.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

	@Autowired
	private PaymentRepository repository;

	@Test
	void findTopByBookingIdOrderByCreatedAtDesc_returnsLatest() throws Exception {
		Payment p1 = new Payment();
		p1.setBookingId(50L);
		p1.setAmount(new BigDecimal("10.00"));
		p1.setTransactionReference("t1");
		p1.setStatus(PaymentStatus.PENDING);
		repository.saveAndFlush(p1);

		Thread.sleep(10);

		Payment p2 = new Payment();
		p2.setBookingId(50L);
		p2.setAmount(new BigDecimal("20.00"));
		p2.setTransactionReference("t2");
		p2.setStatus(PaymentStatus.PENDING);
		repository.saveAndFlush(p2);

		Optional<Payment> top = repository.findTopByBookingIdOrderByCreatedAtDesc(50L);
		assertThat(top).isPresent();
		assertThat(top.get().getTransactionReference()).isEqualTo("t2");
	}

	@Test
	void existsByBookingIdAndStatusIn_and_findByBookingIdOrder() {
		Payment s1 = new Payment();
		s1.setBookingId(60L);
		s1.setAmount(new BigDecimal("30.00"));
		s1.setTransactionReference("s1");
		s1.setStatus(PaymentStatus.SUCCESS);
		repository.saveAndFlush(s1);

		Payment p = new Payment();
		p.setBookingId(60L);
		p.setAmount(new BigDecimal("5.00"));
		p.setTransactionReference("p1");
		p.setStatus(PaymentStatus.PENDING);
		repository.saveAndFlush(p);

		boolean exists = repository.existsByBookingIdAndStatusIn(
				60L,
				List.of(PaymentStatus.SUCCESS, PaymentStatus.PENDING)
		);
		assertThat(exists).isTrue();

		List<Payment> history = repository.findByBookingIdOrderByCreatedAtDesc(60L);
		assertThat(history).isNotEmpty();
		assertThat(history.get(0).getTransactionReference()).isNotNull();
	}

	@Test
	void findByTransactionReference_and_stats_queries() {
		Payment a = new Payment();
		a.setBookingId(70L);
		a.setAmount(new BigDecimal("40.00"));
		a.setTransactionReference("tx_a");
		a.setStatus(PaymentStatus.SUCCESS);
		repository.saveAndFlush(a);

		Payment b = new Payment();
		b.setBookingId(71L);
		b.setAmount(new BigDecimal("60.00"));
		b.setTransactionReference("tx_b");
		b.setStatus(PaymentStatus.SUCCESS);
		repository.saveAndFlush(b);

		Optional<Payment> found = repository.findByTransactionReference("tx_a");
		assertThat(found).isPresent();
		assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("40.00"));

		List<Object[]> grouped = repository.countGroupedByStatus();
		assertThat(grouped).isNotEmpty();

		BigDecimal sum = repository.sumSuccessfulPayments();
		assertThat(sum).isEqualByComparingTo(new BigDecimal("100.00"));
	}
}