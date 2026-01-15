package com.eventhub.dao.service;

import com.eventhub.dao.repository.alloydb.OrganizationRepository;
import com.eventhub.dao.repository.alloydb.WorkspaceRepository;
import com.eventhub.model.Organization;
import com.eventhub.model.Workspace;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WorkspaceUtil extends BaseUtil {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(WorkspaceUtil.class,
                        WorkspaceRepository.class,
                        OrganizationRepository.class);
        WorkspaceRepository service = context.getBean(WorkspaceRepository.class);
        OrganizationRepository organizationRepository = context.getBean(OrganizationRepository.class);
        Workspace workspace = new Workspace();
        workspace.setOrganization(organizationRepository.findById(1L).get());
        workspace.setName("Dev");
        service.save(workspace);
    }
}
