package io.jmix.samples.restservice.app;

import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import io.jmix.samples.restservice.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SampleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SampleDataInitializer.class);
    private final DataManager dataManager;

    public SampleDataInitializer(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventListener(ApplicationStartedEvent.class)
    @Authenticated
    public void init() {
        List<Customer> customers = dataManager.load(Customer.class).all().maxResults(1).list();
        if (!customers.isEmpty()) {
            log.info("Customers already exist, skipping initialization");
            return;
        }

        createCustomers();
    }

    public void createCustomers() {
        log.info("Creating customers");
        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("Robert");
        customer.setLastName("Taylor");
        customer.setEmail("robert@example.com");
        dataManager.save(customer);
        log.info("Customers created");
    }
}