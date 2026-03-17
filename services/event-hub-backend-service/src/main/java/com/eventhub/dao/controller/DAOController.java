package com.eventhub.dao.controller;

import java.util.List;
import java.util.Optional;

import com.eventhub.dao.repository.alloydb.*;
import com.eventhub.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class DAOController {
	
	@Autowired
	private OrganizationRepository orgRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TargetRepository targetRepository;

	@Autowired RoleRepository roleRepository;

	@RequestMapping(value="/roles", method = RequestMethod.GET)
	public List<Role> getRoles() {
		return roleRepository.findAll();
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

	/*
	@RequestMapping(value="/organization/targets", method=RequestMethod.GET)
	public List<Target> getTargets(@RequestParam(name="orgId") String orgId) throws Exception {
		return targetRepository.findByOrgId(orgId);
	}*/
	
	//Consumers
}
