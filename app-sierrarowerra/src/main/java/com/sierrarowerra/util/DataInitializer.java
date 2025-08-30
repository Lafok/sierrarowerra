package com.sierrarowerra.util;

import com.sierrarowerra.domain.BikeRepository;
import com.sierrarowerra.domain.RoleRepository;
import com.sierrarowerra.domain.TariffRepository;
import com.sierrarowerra.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BikeRepository bikeRepository;
    private final RoleRepository roleRepository;
    private final TariffRepository tariffRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, ERole.ROLE_USER));
            roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
            System.out.println("------ Default roles have been added to the database ------");
        }

        if (tariffRepository.count() == 0) {
            Tariff hourly = new Tariff(null, "Standard Hourly", TariffType.HOURLY, new BigDecimal("15.50"), "Standard hourly rate");
            Tariff daily = new Tariff(null, "Standard Daily", TariffType.DAILY, new BigDecimal("100.00"), "Standard daily rate");
            tariffRepository.saveAll(List.of(hourly, daily));
            System.out.println("------ Default tariffs have been added to the database ------");
        }

        if (bikeRepository.count() == 0) {
            Tariff hourlyTariff = tariffRepository.findByName("Standard Hourly").orElseThrow();
            Tariff dailyTariff = tariffRepository.findByName("Standard Daily").orElseThrow();

            Bike bike1 = new Bike(null, "Stels Navigator 500", BikeType.MOUNTAIN, BikeStatus.AVAILABLE, hourlyTariff,
                    List.of("https://via.placeholder.com/400x300.png?text=Stels+Navigator+500"));
            Bike bike2 = new Bike(null, "Merida Speeder 200", BikeType.ROAD, BikeStatus.AVAILABLE, hourlyTariff,
                    List.of("https://via.placeholder.com/400x300.png?text=Merida+Speeder+200"));
            Bike bike3 = new Bike(null, "Schwinn Wayfarer", BikeType.CITY, BikeStatus.AVAILABLE, dailyTariff,
                    List.of("https://via.placeholder.com/400x300.png?text=Schwinn+Wayfarer"));
            Bike bike4 = new Bike(null, "Giant Talon 29", BikeType.MOUNTAIN, BikeStatus.RENTED, dailyTariff,
                    List.of("https://via.placeholder.com/400x300.png?text=Giant+Talon+29"));
            Bike bike5 = new Bike(null, "Trek Domane AL 2", BikeType.ROAD, BikeStatus.MAINTENANCE, hourlyTariff,
                    List.of("https://via.placeholder.com/400x300.png?text=Trek+Domane+AL+2"));


            bikeRepository.saveAll(List.of(bike1, bike2, bike3, bike4, bike5));
            System.out.println("------ Test bikes have been added to the database ------");
        }
    }
}
