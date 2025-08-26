package com.sierrarowerra.util;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.BikeType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BikeRepository bikeRepository;

    @Override
    public void run(String... args) {
        if (bikeRepository.count() == 0) {
            Bike bike1 = new Bike(null, "Stels Navigator 500", BikeType.MOUNTAIN, true);
            Bike bike2 = new Bike(null, "Merida Speeder 200", BikeType.ROAD, true);
            Bike bike3 = new Bike(null, "Schwinn Wayfarer", BikeType.CITY, true);

            bikeRepository.saveAll(List.of(bike1, bike2, bike3));
            System.out.println("------ Test bikes have been added to the database ------");
        }
    }
}
