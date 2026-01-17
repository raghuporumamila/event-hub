package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.EventDefinitionRepository;
import com.eventhub.model.EventDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces/{workspaceId}/eventDefinitions")
public class EventDefinitionController {
    @Autowired
    private EventDefinitionRepository eventDefinitionRepository;

    @RequestMapping(method= RequestMethod.GET)
    public List<EventDefinition> getEventDefinitions(@PathVariable Long orgId,
                                                     @PathVariable Long workspaceId) throws Exception {
        return eventDefinitionRepository.getEventDefinitions(orgId, workspaceId);
    }

    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public EventDefinition getEventDefinition(@RequestParam(name="id") Long id) throws Exception {
        return eventDefinitionRepository.findById(id).get();
    }

    @RequestMapping(method=RequestMethod.POST)
    public void saveEventDefinition(@RequestBody EventDefinition eventDefinition)  throws Exception {
        eventDefinitionRepository.save(eventDefinition);
    }

    @RequestMapping(method=RequestMethod.PUT)
    public void updateEventDefinition(@RequestBody EventDefinition eventDefinition)  throws Exception {
        eventDefinitionRepository.save(eventDefinition);
    }

    @RequestMapping(value="{id}", method=RequestMethod.DELETE)
    public void deleteEventDefinition(@PathVariable Long id)  throws Exception {
        eventDefinitionRepository.delete(eventDefinitionRepository.findById(id).get());
    }
}
