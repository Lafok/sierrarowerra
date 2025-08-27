package com.sierrarowerra.util;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.RoleRepository;
import com.sierrarowerra.model.Bike;
import com.sierrarowerra.model.BikeStatus;
import com.sierrarowerra.model.BikeType;
import com.sierrarowerra.model.ERole;
import com.sierrarowerra.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BikeRepository bikeRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Initialize Bikes
        if (bikeRepository.count() == 0) {
            Bike bike1 = new Bike(null, "Stels Navigator 500", BikeType.MOUNTAIN, BikeStatus.AVAILABLE);
            Bike bike2 = new Bike(null, "Merida Speeder 200", BikeType.ROAD, BikeStatus.AVAILABLE);
            Bike bike3 = new Bike(null, "Schwinn Wayfarer", BikeType.CITY, BikeStatus.AVAILABLE);

            bikeRepository.saveAll(List.of(bike1, bike2, bike3));
            System.out.println("------ Test bikes have been added to the database ------");
        }

        // Initialize Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, ERole.ROLE_USER));
            roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
            System.out.println("------ Default roles have been added to the database ------");
        }
    }
}
