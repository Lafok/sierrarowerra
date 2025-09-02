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

            Bike bike1 = new Bike(null, "Trek Madone SLR 9", BikeType.ROAD, BikeStatus.AVAILABLE, hourlyTariff,
                    List.of(new Image("https://i0.wp.com/bicicletascarmona.com/wp-content/uploads/2024/07/MadoneSLR9AXS-25-46151-D-Primary.webp?fit=1920%2C1440&ssl=1", true)));
            Bike bike2 = new Bike(null, "Merida NINETY-SIX 9000", BikeType.MOUNTAIN, BikeStatus.AVAILABLE, hourlyTariff,
                    List.of(new Image("https://d2lljesbicak00.cloudfront.net/merida-v2/crud-zoom-img/master/bikes/2025/NINETY-SIX_9000_gryslv_MY25.tif?p3", true)));
            Bike bike3 = new Bike(null, "Canyon Commuter mid-step ", BikeType.CITY, BikeStatus.AVAILABLE, hourlyTariff,
                    List.of(new Image("https://www.canyon.com/dw/image/v2/BCML_PRD/on/demandware.static/-/Sites-canyon-master/default/dw79321dff/images/full/2025_FULL_/2025/2025_FULL_commuter_mds_4203_U023_P07.jpg?sw=1300&sfrm=png&q=90&bgcolor=F2F2F2", true)));
            Bike bike4 = new Bike(null, "Santa Cruz v10", BikeType.MOUNTAIN, BikeStatus.RENTED, hourlyTariff,
                    List.of(new Image("https://bikebrothers.es/435-home_default/santa-cruz-v-cc-kit-dh-x.jpg", true)));
            Bike bike5 = new Bike(null, "Trek Domane AL 2", BikeType.ROAD, BikeStatus.MAINTENANCE, hourlyTariff,
                    List.of(new Image("https://bicicletascarmona.com/wp-content/uploads/2022/03/domane_al.jpeg", true)));


            bikeRepository.saveAll(List.of(bike1, bike2, bike3, bike4, bike5));
            System.out.println("------ Test bikes have been added to the database ------");
        }
    }
}
