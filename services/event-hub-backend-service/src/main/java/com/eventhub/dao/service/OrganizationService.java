package com.eventhub.dao.service;

import com.eventhub.dao.repository.alloydb.OrganizationRepository;
import com.eventhub.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    public Organization createOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }
}
