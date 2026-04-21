package com.swer313.projectstep1.catalog.room;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface RoomTypeService {

    RoomTypeResponseDto getById(Long id);

    PagedResponse<RoomTypeResponseDto> listAll(
            Pageable pageable,
            Long hotelId,
            String name,
            BedType bedType,
            Integer bedCountMin, Integer bedCountMax,
            Integer capacityMin, Integer capacityMax,
            Integer maxAdultsMin, Integer maxAdultsMax,
            Integer maxChildrenMin, Integer maxChildrenMax,
            BigDecimal priceMin, BigDecimal priceMax,
            String q
    );

    RoomTypeResponseDto getByIdAdmin(Long hotelId, Long id);

    RoomTypeResponseDto create(Long hotelId, RoomTypeRequestDto dto);

    RoomTypeResponseDto update(Long hotelId, Long id, RoomTypeRequestDto dto);

    void delete(Long hotelId, Long id);

    RoomTypeResponseDto changeStatus(Long hotelId, Long id, RoomTypeStatus status);

    //RoomTypeResponseDto replaceImages(Long hotelId, Long id, List<String> images);

  //  RoomTypeResponseDto addImage(Long hotelId, Long id, String url);

   // RoomTypeResponseDto removeImage(Long hotelId, Long id, int index);

    RoomTypeResponseDto replacePolicies(Long hotelId, Long id, String policies);

    RoomTypeResponseDto clone(Long hotelId, Long id);

    BulkStatusResult bulkStatus(Long hotelId, List<Long> ids, RoomTypeStatus status);

    PagedResponse<RoomTypeResponseDto> listByHotel(
            Long hotelId, Pageable pageable,
            String name, BedType bedType,
            Integer bedCountMin, Integer bedCountMax,
            Integer capacityMin, Integer capacityMax,
            Integer maxAdultsMin, Integer maxAdultsMax,
            Integer maxChildrenMin, Integer maxChildrenMax,
            BigDecimal priceMin, BigDecimal priceMax,
            RoomTypeStatus status, String q
    );

    PagedResponse<RoomTypeMinimalDto> minimal(Long hotelId, RoomTypeStatus status, int page, int size);

    List<RoomTypeMinimalDto> suggest(Long hotelId, String q);

    boolean exists(Long id);

    RoomTypeResponseDto replaceAmenities(Long roomTypeId, Set<Long> amenityIds);

    RoomTypeResponseDto addAmenity(Long roomTypeId, Long amenityId);

    RoomTypeResponseDto removeAmenity(Long roomTypeId, Long amenityId);

    record RoomTypeMinimalDto(Long id, String name) {}

    record BulkStatusResult(List<Long> updatedIds, List<Long> notFoundIds, RoomTypeStatus status) {}
    List<RoomTypeImageResponseDto> uploadImages(Long roomTypeId, List<MultipartFile> files);
    List<RoomTypeImageResponseDto> getRoomTypeImages(Long roomTypeId);
    void deleteRoomTypeImage(Long roomTypeId, Long imageId);
}