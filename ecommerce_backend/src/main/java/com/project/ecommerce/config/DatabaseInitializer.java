package com.project.ecommerce.config;

import com.project.ecommerce.model.Role;
import com.project.ecommerce.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(Role.ERole.ROLE_USER);
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(Role.ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);

            System.out.println("Roles initialized successfully");
        }
    }
}