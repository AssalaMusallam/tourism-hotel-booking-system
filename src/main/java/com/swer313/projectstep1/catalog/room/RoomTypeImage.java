package com.swer313.projectstep1.catalog.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "room_type_images")
public class RoomTypeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 255)
    @Column(length = 255)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    public RoomTypeImage() {}

    public Long getId()                          { return id; }
    public String getImageUrl()                  { return imageUrl; }
    public void setImageUrl(String imageUrl)     { this.imageUrl = imageUrl; }
    public String getFileName()                  { return fileName; }
    public void setFileName(String fileName)     { this.fileName = fileName; }
    public RoomType getRoomType()                { return roomType; }
    public void setRoomType(RoomType roomType)   { this.roomType = roomType; }
}