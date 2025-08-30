package com.sierrarowerra.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bikes")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private BikeType type;

    @Enumerated(EnumType.STRING)
    private BikeStatus status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false) // Changed to EAGER to fix StaleObjectStateException
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "bike_images", joinColumns = @JoinColumn(name = "bike_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();
}
