package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.EventRepository;
import com.eventhub.model.Event;
import com.eventhub.model.EventCountsByDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces/{workspaceId}/events")
public class EventController {
    @Autowired
    private EventRepository eventRepository;

    @RequestMapping(method= RequestMethod.GET)
    public List<Event> getEvents(@PathVariable Long orgId,
                                 @PathVariable Long workspaceId)  throws Exception {
        return eventRepository.getEvents(orgId, workspaceId);
    }
    /*
    @RequestMapping(value="/organization/eventCountsForPast7Days", method=RequestMethod.GET)
    public List<EventCountsByDay> getEventCountsForPast7Days(@RequestParam(name="orgId") String orgId, @RequestParam(name="workspace") String workspace)  throws Exception {
        return eventRepository.findEventCountsForPast7Days(orgId, workspace);
    }*/
}
