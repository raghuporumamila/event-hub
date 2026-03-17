package com.eventhub.dao.service;

import com.eventhub.dao.repository.alloydb.OrganizationRepository;
import com.eventhub.dao.repository.alloydb.RoleRepository;
import com.eventhub.dao.repository.alloydb.UserRepository;
import com.eventhub.dao.repository.alloydb.WorkspaceRepository;
import com.eventhub.model.Organization;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.model.Workspace;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

public class UserUtil extends BaseUtil {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(UserUtil.class,
                        RoleRepository.class,
                        UserRepository.class,
                        OrganizationRepository.class,
                        WorkspaceRepository.class);
        UserRepository service = context.getBean(UserRepository.class);
        User user = new User();
        RoleRepository roleRepository = context.getBean(RoleRepository.class);
        Optional<Role> role = roleRepository.findById(1L);
        role.ifPresent(user::setRole);
        user.setEmail("raghu.porumamilla@gmail.com");
        user.setName("Raghu Porumamilla");
        user.setPassword("$2a$10$FXqs6EVGQ.F14teFQuoEHe30QnhyPcK0cWF2P5vWlhRrmJ9I1kDUq");
        OrganizationRepository organizationRepository = context.getBean(OrganizationRepository.class);
        user.setOrganization(organizationRepository.findById(1L).get());
        WorkspaceRepository workspaceRepository = context.getBean(WorkspaceRepository.class);
        user.setDefaultWorkspace(workspaceRepository.findById(1L).get());
        service.save(user);
    }
}
