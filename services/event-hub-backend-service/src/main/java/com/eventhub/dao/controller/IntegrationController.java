package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.IntegrationRepository;
import com.eventhub.model.Integration;
import com.eventhub.model.Organization;

import com.eventhub.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces/{workspaceId}/integrations")
public class IntegrationController {
    @Autowired
    private IntegrationRepository integrationRepository;

    @RequestMapping(method= RequestMethod.POST)
    public void saveIntegration(@PathVariable Long orgId,
                               @PathVariable Long workspaceId,
                               @RequestBody Integration integration)  throws Exception {
        integration.setOrganization(new Organization(orgId, null, null, null, null,
                null, null, null));
        integration.setWorkspace(new Workspace(workspaceId, null, null));
        integrationRepository.save(integration);
    }

    @RequestMapping(method= RequestMethod.GET)
    public List<Integration> getIntegrations(@PathVariable Long orgId,
                                             @PathVariable Long workspaceId) {
        return integrationRepository.getIntegrations(orgId, workspaceId);
    }

    @RequestMapping(method= RequestMethod.GET, params = "sourceId")
    public Integration getIntegrationBySourceId(@PathVariable Long orgId,
                                      @PathVariable Long workspaceId,
                                      @RequestParam Long sourceId) {
        List<Integration> integrations = integrationRepository.getIntegration(orgId, workspaceId, sourceId);
        Integration integration = null;
        if (integrations != null && !integrations.isEmpty()) {
            integration = integrations.get(0);
        }
        return integration;
    }
}
