package com.swer313.projectstep1.catalog.hotel;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByName(String name);

    Optional<Hotel> findByName(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // ── Public catalog (ACTIVE only)

    @Query("""
           select distinct h.city
           from Hotel h
           where h.status = :status
             and h.city is not null and h.city <> ''
           order by h.city asc
           """)
    List<String> findDistinctActiveCities(@Param("status") Hotel.Status status);

    @Query("""
           select distinct h.country
           from Hotel h
           where h.status = :status
             and h.country is not null and h.country <> ''
           order by h.country asc
           """)
    List<String> findDistinctActiveCountries(@Param("status") Hotel.Status status);

    @Query("""
           select h.name
           from Hotel h
           where h.status = :status
             and lower(h.name) like lower(concat('%', :q, '%'))
           order by h.name asc
           """)
    List<String> findActiveNameContaining(@Param("q") String q,
                                          @Param("status") Hotel.Status status,
                                          Pageable pageable);

    // ── Admin (all statuses)

    @Query("""
           select distinct h.city
           from Hotel h
           where h.city is not null and h.city <> ''
           order by h.city asc
           """)
    List<String> findDistinctCities();

    @Query("""
           select distinct h.country
           from Hotel h
           where h.country is not null and h.country <> ''
           order by h.country asc
           """)
    List<String> findDistinctCountries();
}
