package com.se114p12.backend.seeders;

import com.se114p12.backend.enums.RoleName;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

  private final RoleInitializer roleInitializer;
  private final UserInitializer userInitializer;
  private final RoleRepository roleRepository;
  private final CategoryInitializer categoryInitializer;
  private final PromotionInitializer promotionInitializer;
  private final ShipperInitializer shipperInitializer;

  @Transactional
  @Override
  public void run(ApplicationArguments args) throws Exception {

    // Initialize roles
    roleInitializer.initializeRoles();

    // Initialize users
    // userInitializer.initializeAdminUser(
    //     roleRepository
    //         .findByName(RoleName.ADMIN.getValue())
    //         .orElseThrow(() -> new IllegalStateException("Admin role not found")));

    // categoryInitializer.initializeCategories();
    // shipperInitializer.initializeShippers();
    // promotionInitializer.initializePromotions();
  }
}
