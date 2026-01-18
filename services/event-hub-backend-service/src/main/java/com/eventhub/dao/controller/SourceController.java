package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.OrganizationRepository;
import com.eventhub.dao.repository.alloydb.SourceRepository;
import com.eventhub.dao.repository.alloydb.WorkspaceRepository;
import com.eventhub.model.Organization;
import com.eventhub.model.Source;
import com.eventhub.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces/{workspaceId}/sources")
public class SourceController {
    @Autowired
    private SourceRepository sourceTypeRepository;

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @RequestMapping(method=RequestMethod.POST)
    public void saveSourceType(@PathVariable Long orgId, @PathVariable Long workspaceId, @RequestBody Source sourceType)  throws Exception {
        Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(sourceType::setOrganization);
        Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
        workspace.ifPresent(sourceType::setWorkspace);
        sourceTypeRepository.save(sourceType);
    }

    @RequestMapping(method=RequestMethod.DELETE)
    public void deleteSourceType(@PathVariable Long orgId, @RequestBody Source sourceType)  throws Exception {
        Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(sourceType::setOrganization);
        sourceTypeRepository.delete(sourceType);
    }

    @RequestMapping(value = "{sourceId}", method=RequestMethod.GET)
    public Source getSource(@PathVariable Long sourceId)  throws Exception {
        return sourceTypeRepository.findById(sourceId).get();
    }

    @RequestMapping(method=RequestMethod.GET)
    public List<Source> getSourceTypes(@PathVariable Long orgId,
                                       @PathVariable Long workspaceId)  throws Exception {
        return sourceTypeRepository.getSources(orgId, workspaceId);
    }
}
