package com.eventhub.dao.controller;

import com.eventhub.dao.repository.alloydb.ConsumerRepository;
import com.eventhub.model.Consumer;
import com.eventhub.model.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/organizations/{orgId}/workspaces/{workspaceId}/consumers")
public class ConsumerController {
    @Autowired
    private ConsumerRepository consumerRepository;

    @RequestMapping(method= RequestMethod.GET)
    public List<Consumer> getConsumers(@PathVariable Long orgId,
                                       @PathVariable Long workspaceId) {
        return consumerRepository.getConsumers(orgId, workspaceId);
    }

    @RequestMapping(method= RequestMethod.POST)
    public Consumer createConsumer(@RequestBody Consumer consumer) {
        return consumerRepository.save(consumer);
    }
}
