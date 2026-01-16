package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.OrganizationRepository;
import com.eventhub.dao.repository.alloydb.TargetRepository;
import com.eventhub.dao.repository.alloydb.WorkspaceRepository;
import com.eventhub.model.Organization;
import com.eventhub.model.Target;
import com.eventhub.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces/{workspaceId}/targets")
public class TargetController {
    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @RequestMapping(method= RequestMethod.POST)
    public void saveTarget(@PathVariable Long orgId,
                           @PathVariable Long workspaceId,
                           @RequestBody Target target)  throws Exception {
        Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(target::setOrganization);
        Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
        workspace.ifPresent(target::setWorkspace);
        targetRepository.save(target);
    }

    /*
    @RequestMapping(method=RequestMethod.DELETE)
    public void deleteSourceType(@PathVariable Long orgId, @RequestBody Source sourceType)  throws Exception {
        Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(sourceType::setOrganization);
        sourceTypeRepository.delete(sourceType);
    }
     */

    @RequestMapping(method=RequestMethod.GET)
    public List<Target> getTargets(@PathVariable Long orgId,
                                       @PathVariable String workspaceId)  throws Exception {
        return targetRepository.getTargets(orgId, workspaceId);
    }
}
