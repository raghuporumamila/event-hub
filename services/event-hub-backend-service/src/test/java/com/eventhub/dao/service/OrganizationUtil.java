package com.eventhub.dao.service;

import com.eventhub.model.Organization;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


public class OrganizationUtil extends BaseUtil {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(OrganizationUtil.class, OrganizationService.class);
        OrganizationService service = context.getBean(OrganizationService.class);
        Organization organization = new Organization();
        organization.setName("Penny Technologies");
        organization.setAddress("100 1st AVE");
        organization.setCity("Cincinnati");
        organization.setPostalCode("45244");
        organization.setCountry("USA");
        organization.setState("OH");
        service.createOrganization(organization);
    }
}
