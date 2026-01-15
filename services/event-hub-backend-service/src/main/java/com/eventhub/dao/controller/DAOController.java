package com.eventhub.dao.controller;

import java.util.List;
import java.util.Optional;

import com.eventhub.dao.repository.alloydb.*;
import com.eventhub.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eventhub.dao.util.RepositoryUtil;

@RestController
public class DAOController {

	@Autowired
	private SourceRepository sourceRepository;

	@Autowired
	private SourceTypeRepository sourceTypeRepository;
	
	@Autowired
	private OrganizationRepository orgRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TargetRepository targetRepository;
	
	@Autowired
	private ConsumerRepository consumerRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private WorkspaceRepository workspaceRepository;

	@Autowired RoleRepository roleRepository;

	@RequestMapping(value="/roles", method = RequestMethod.GET)
	public List<Role> getRoles() {
		return roleRepository.findAll();
	}

	@RequestMapping(value="/sources", method=RequestMethod.POST)
	public void saveSource(@RequestBody Source source) throws Exception {
		sourceRepository.save(source);
	}
	

	
	@RequestMapping(value="/sourceTypes", method=RequestMethod.GET)
	public List<SourceType> getSourceTypes() throws Exception {
		return sourceTypeRepository.findAll();
	}

	@RequestMapping(value="/organizations/{orgId}/{workspace}/sourceTypes", method=RequestMethod.GET)
	public List<Source> getOrgAndWorkspaceSourceTypes(@PathVariable String orgId,
								   @PathVariable String workspace) throws Exception {
		return sourceTypeRepository.findByOrgId(orgId, workspace);
	}

	@RequestMapping(value="/organizations/{orgId}/{workspace}/sources", method=RequestMethod.GET)
	public List<Source> getSources(@PathVariable String orgId,
								   @PathVariable String workspace) throws Exception {
		return sourceRepository.findByOrgId(orgId, workspace);
	}
	
	//org releated methods
	@RequestMapping(value="/organizations", method=RequestMethod.POST)
	public Organization saveOrganization(@RequestBody Organization org) throws Exception {
		return orgRepository.save(org);
	}
	
	@RequestMapping(value="/organizations", method=RequestMethod.GET)
	public List<Organization> getOrganizations() throws Exception {
		return orgRepository.findAll();
	}
    /*
	@RequestMapping(value="/organizationBySourceKey", method=RequestMethod.GET)
	public Organization getOrganization(@RequestParam(name="sourceKey") String sourceKey ) throws Exception {
		return orgRepository.findBySourceKey(sourceKey);
	}
	*/
	//User releated methods
	@RequestMapping(value="/organizations/{orgId}/users", method=RequestMethod.POST)
	public User saveUser(@PathVariable Long orgId, @RequestBody User user) throws Exception {
		Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(user::setOrganization);
		return userRepository.save(user);
	}


	@RequestMapping(value="/organizations/{orgId}/users/{userId}/workspaces", method=RequestMethod.PUT)
	public void changeWorkspace(@PathVariable(name="orgId") Long orgId,
								@PathVariable(name="userId") Long userId,
								@RequestBody Workspace workspace) throws Exception {

		Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(workspace::setOrganization);
		workspace = workspaceRepository.save(workspace);
		Optional<User> user = userRepository.findById(userId);
		if (user.isPresent()) {
			user.get().setDefaultWorkspace(workspace);
		}
        user.ifPresent(value -> userRepository.save(value));
	}

	
	@RequestMapping(value="/organizations/{orgId}/users", method=RequestMethod.GET)
	public List<User> getUsers(@PathVariable String orgId) throws Exception {
		return userRepository.findAllByOrgId(orgId);
	}
	
	@RequestMapping(value="/users", method=RequestMethod.GET)
	public User getUser(@RequestParam(name="email") String email) throws Exception {
		return userRepository.findByEmail(email);
	}
	
	//Target related methodsx
	@RequestMapping(value="/targets", method=RequestMethod.GET)
	public List<Target> getTargets() throws Exception {
		return targetRepository.findAll();
	}
	
	@RequestMapping(value="/organization/targets", method=RequestMethod.GET)
	public List<Target> getTargets(@RequestParam(name="orgId") String orgId) throws Exception {
		return targetRepository.findByOrgId(orgId);
	}
	
	//Consumers
	@RequestMapping(value="/organization/consumers", method=RequestMethod.GET)
	public List<Consumer> getConsumers(@RequestParam(name="orgId") String orgId, @RequestParam(name="workspace") String workspace) throws Exception {
		return consumerRepository.findByOrgId(orgId, workspace);
	}
	
	@RequestMapping(value="/organization/eventDefinitions", method=RequestMethod.GET)
	public List<EventDefinition> getEventDefinitions(@RequestParam(name="orgId") String orgId, @RequestParam(name="workspace") String workspace) throws Exception {
		return eventRepository.findDefinitionsByOrgId(orgId, workspace);
	}
	
	@RequestMapping(value="/eventDefinition", method=RequestMethod.GET)
	public EventDefinition getEventDefinition(@RequestParam(name="id") String id) throws Exception {
		return eventRepository.findDefinition(id);
	}
	
	@RequestMapping(value="/organization/eventDefinition", method=RequestMethod.GET)
	public EventDefinition getEventDefinition(@RequestParam(name="eventName") String eventName, @RequestParam(name="orgId") String orgId, @RequestParam(name="workspace") String workspace) throws Exception {
		return eventRepository.findDefinitionByOrgId(orgId, workspace, eventName);
	}
	
	@RequestMapping(value="/organization/eventDefinition", method=RequestMethod.PUT)
	public void saveEventDefinition(@RequestBody EventDefinition eventDefinition)  throws Exception {
		eventDefinition.setId(RepositoryUtil.getDocumentId());
		eventRepository.saveDefinition(eventDefinition);
	}
	
	@RequestMapping(value="/organization/eventDefinition", method=RequestMethod.POST)
	public void updateEventDefinition(@RequestBody EventDefinition eventDefinition)  throws Exception {
		eventRepository.updateDefinition(eventDefinition);
	}
	
	@RequestMapping(value="/organization/eventDefinition", method=RequestMethod.DELETE)
	public void deleteEventDefinition(@RequestParam(name="id") String id)  throws Exception {
		eventRepository.deleteDefinition(id);
	}
	
	@RequestMapping(value="/organization/workspace", method=RequestMethod.PUT)
	public void saveWorkspace(@RequestBody Workspace orgWorkspace)  throws Exception {
		workspaceRepository.save(orgWorkspace);
	}
	
	@RequestMapping(value="/organization/workspaces", method=RequestMethod.GET)
	public List<Workspace> getWorkspaces(@RequestParam(name="orgId") String orgId)  throws Exception {
		return workspaceRepository.getWorkspaces(orgId);
	}
	
	@RequestMapping(value="/organizations/{orgId}/sourceTypes", method=RequestMethod.POST)
	public void saveSourceType(@PathVariable Long orgId, @RequestBody SourceType sourceType)  throws Exception {
		Optional<Organization> organization = orgRepository.findById(orgId);
        organization.ifPresent(sourceType::setOrganization);
		sourceTypeRepository.save(sourceType);
	}
	
	@RequestMapping(value="/organizations/{orgId}/sourceType", method=RequestMethod.DELETE)
	public void deleteSourceType(@PathVariable Long orgId, @RequestBody SourceType sourceType)  throws Exception {
		Optional<Organization> organization = orgRepository.findById(orgId);
		organization.ifPresent(sourceType::setOrganization);
		sourceTypeRepository.delete(sourceType);
	}
	
	@RequestMapping(value="/organization/{orgId}/sourceTypes", method=RequestMethod.GET)
	public List<SourceType> getSourceTypes(@PathVariable Long orgId,
										   @RequestParam(name="workspace") String workspace)  throws Exception {
		Optional<Organization> organization = orgRepository.findById(orgId);
        return organization.map(Organization::getSourceTypes).orElse(null);
    }
	
	@RequestMapping(value="/organization/events", method=RequestMethod.GET)
	public List<Event> getEvents(@RequestParam(name="orgId") String orgId, @RequestParam(name="workspace") String workspace)  throws Exception {
		return eventRepository.getLatestEvents(orgId, workspace);
	}
	
	@RequestMapping(value="/organization/eventCountsForPast7Days", method=RequestMethod.GET)
	public List<EventCountsByDay> getEventCountsForPast7Days(@RequestParam(name="orgId") String orgId, @RequestParam(name="workspace") String workspace)  throws Exception {
		return eventRepository.findEventCountsForPast7Days(orgId, workspace);
	}
}
