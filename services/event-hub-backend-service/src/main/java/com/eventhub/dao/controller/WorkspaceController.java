package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.OrganizationRepository;
import com.eventhub.dao.repository.alloydb.UserRepository;
import com.eventhub.dao.repository.alloydb.WorkspaceRepository;
import com.eventhub.model.Organization;
import com.eventhub.model.User;
import com.eventhub.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces")
public class WorkspaceController {

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(method= RequestMethod.GET)
    public List<Workspace> getWorkspaces(@PathVariable String orgId) {
        return workspaceRepository.getWorkspaces(orgId);
    }

    @RequestMapping(value="/{workspaceId}", method= RequestMethod.GET)
    public Workspace getWorkspace(@PathVariable Long workspaceId) {
        return workspaceRepository.findById(workspaceId).get();
    }

    @RequestMapping(method=RequestMethod.PUT)
    public void saveWorkspace(@RequestBody Workspace orgWorkspace)  throws Exception {
        workspaceRepository.save(orgWorkspace);
    }

    @RequestMapping(value="/{workspaceId}", method=RequestMethod.PUT)
    public void changeWorkspace(@PathVariable Long orgId,
                                @PathVariable Long workspaceId,
                                @RequestParam Long userId) {

        Optional<Organization> organization = orgRepository.findById(orgId);
        Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
        organization.ifPresent(workspace.get()::setOrganization);
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(value -> value.setDefaultWorkspace(workspace.get()));
        user.ifPresent(value -> userRepository.save(value));
    }
}
