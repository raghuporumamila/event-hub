package com.eventhub.site;

import java.util.Arrays;
import java.util.List;

import com.eventhub.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.userdetails.UserDetails;
import com.eventhub.site.config.ApiEndPointUri;
import com.eventhub.site.form.IntegrationsForm;
import com.eventhub.site.form.RegistrationForm;

@Controller
@SessionAttributes("user")

@RequestMapping("/site/v1")
public class SiteController {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ApiEndPointUri apiEndPointUri;
	
	
	@GetMapping(value = "/index")
	public String index() {
		return "index";
	}

	@GetMapping(value = "/dashboard")
	public ModelAndView dashboard() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) auth.getPrincipal();
		User siteUser = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/users?email=" + userDetails.getUsername(), HttpMethod.GET, null,
				new ParameterizedTypeReference<User>() {
				}).getBody();
		ModelAndView modelAndView = new ModelAndView();
        assert siteUser != null;
        siteUser.getRole().setName(siteUser.getRole().getName().replace("ROLE_", ""));
		modelAndView.addObject("user", siteUser);
		modelAndView.setViewName("dashboard");
		return modelAndView;
	}


	@GetMapping(value = "/events")
	public String events() {
		return "events";
	}

	/*
	@GetMapping(value = "/definitions")
	public ModelAndView definitions(@ModelAttribute("user") User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("manageDefinitions");
		
		List<EventDefinition> definitions = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/eventDefinitions?orgId=" +
				user.getOrganization().getId() + "&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<EventDefinition>>() {
				}).getBody();

		List<Source> sources = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/sourceTypes?orgId=" + user.getOrganization().getId() +
				"&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Source>>() {
				}).getBody();

		for (EventDefinition definition : definitions) {
			for (Source source : sources) {
				if (definition.getSource().getId().longValue() == source.getId().longValue()) {
					definition.setSource(source);
					break;
				}
			}
		}
		modelAndView.addObject("definitions", definitions);
		modelAndView.addObject("sources", sources);

		return modelAndView;
	}*/

	/*
	@PostMapping(value = "/createDefinition")
	public ModelAndView createDefinition(@ModelAttribute("user") User user, @ModelAttribute EventDefinition eventDefinition) {
		System.out.println("Entered in createDefinition == " + eventDefinition.getEventName());
		eventDefinition.setOrganization(user.getOrganization());
		eventDefinition.setWorkspace(user.getDefaultWorkspace());
		
		HttpEntity<EventDefinition> requestUpdate = new HttpEntity<>(eventDefinition, (HttpHeaders) null);
		ResponseEntity<String> response = restTemplate.exchange( apiEndPointUri.getDaoApiEndpoint() + "/organization/eventDefinition", HttpMethod.PUT, requestUpdate , String.class );
	
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}

		return definitions(user);
	}
	
	@GetMapping(value = "/editDefinition")
	public ModelAndView editDefinition(@ModelAttribute("user") User user, @RequestParam(name="id") String id) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("editDefinition");

		EventDefinition definition = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "eventDefinition?id=" +
				id, HttpMethod.GET, null,
				new ParameterizedTypeReference<EventDefinition>() {
				}).getBody();

		List<Source> sources = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/sourceTypes?orgId=" + user.getOrganization().getId() +
				"&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Source>>() {
				}).getBody();

		for (Source source : sources) {
			if (definition.getSource().getId().longValue() == source.getId().longValue()) {
				definition.setSource(source);
				break;
			}
		}
		modelAndView.addObject("definition", definition);
		modelAndView.addObject("sources", sources);

		return modelAndView;
	}
	
	@PostMapping(value = "/updateDefinition")
	@ResponseStatus(HttpStatus.OK)
	public void updateDefinition(@ModelAttribute("user") User user, @ModelAttribute EventDefinition eventDefinition) {
		System.out.println(eventDefinition.getSchema());
		EventDefinition definitionFromDB = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "eventDefinition?id=" +
				eventDefinition.getId(), HttpMethod.GET, null,
				new ParameterizedTypeReference<EventDefinition>() {
				}).getBody();
		eventDefinition.setEventName(definitionFromDB.getEventName());
		eventDefinition.setSource(definitionFromDB.getSource());
		eventDefinition.setWorkspace(definitionFromDB.getWorkspace());
		eventDefinition.setOrganization(user.getOrganization());
		eventDefinition.setWorkspace(user.getDefaultWorkspace());
		
		HttpEntity<EventDefinition> requestUpdate = new HttpEntity<>(eventDefinition, (HttpHeaders) null);
		ResponseEntity<String> response = restTemplate.exchange( apiEndPointUri.getDaoApiEndpoint() + "/organization/eventDefinition", HttpMethod.POST, requestUpdate , String.class );
	
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}

		//return definitions(user);
	}
	
	@PostMapping(value = "/deleteDefinition")
	public ModelAndView deleteDefinition(Model model, @ModelAttribute("user") User user, @RequestParam(name="id") String id) {
		ResponseEntity<String> response = restTemplate.exchange( apiEndPointUri.getDaoApiEndpoint() + "/organization/eventDefinition?id=" + id, HttpMethod.DELETE, null , String.class );
	
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}
		return definitions(user);
	}*/

	@GetMapping(value = "/sources")
	public String sources(Model model, @ModelAttribute("user") User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<SourceType> orgSourceTypes = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organizations/" +
						user.getOrganization().getId() + "/" + user.getDefaultWorkspace().getName() + "/sourceTypes"
						, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<SourceType>>() {
				}).getBody();
		model.addAttribute("orgSourceTypes", orgSourceTypes);

		List<SourceType> sourceTypes = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/sourceTypes", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<SourceType>>() {
				}).getBody();
		model.addAttribute("sourceTypes", sourceTypes);

		return "manageSources";
	}
	
	@GetMapping(value = "/sourceType")
	public String source(Model model, @ModelAttribute("user") User user, @RequestParam(name="id") String id) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<SourceType> orgSourceTypes = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/sourceTypes?orgId=" + user.getOrganization().getId() +
				"&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<SourceType>>() {
				}).getBody();
		for (SourceType orgSourceType : orgSourceTypes) {
			if (orgSourceType.getId().equals(id)) {
				model.addAttribute("orgSourceType", orgSourceType);
				break;
			}
		}
		

		return "sourceType";
	}
	/*
	@PostMapping(value = "/createSource")
	public String createSource(Model model, @ModelAttribute("user") User user, @RequestParam(name="name") String name, @RequestParam(name="type") String type) {
		SourceType sourceType = new SourceType();
		sourceType.setName(name);
		sourceType.setType(type);
		sourceType.setWorkspace(user.getDefaultWorkspace());
		HttpEntity<SourceType> requestUpdate = new HttpEntity<>(sourceType, (HttpHeaders) null);
		
		ResponseEntity<String> response = restTemplate.exchange( apiEndPointUri.getDaoApiEndpoint() + "/organization/sourceType?orgId=" + user.getOrganization().getId(), HttpMethod.PUT, requestUpdate , String.class );
	
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}
		return sources(model, user);
	}

	@PostMapping(value = "/deleteSource")
	public String deleteSource(Model model, @ModelAttribute("user") User user, @RequestParam(name="id") String id) {
		SourceType sourceType = new SourceType();
		sourceType.setId(id);
		HttpEntity<SourceType> requestUpdate = new HttpEntity<>(sourceType, (HttpHeaders) null);
		ResponseEntity<String> response = restTemplate.exchange( apiEndPointUri.getDaoApiEndpoint() + "/organization/sourceType?orgId=" + user.getOrganization().getId(), HttpMethod.DELETE, requestUpdate , String.class );
	
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}
		return sources(model, user);
	}
*/
	@GetMapping(value = "/targets")
	public String targets(Model model) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<Target> targets = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/targets", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Target>>() {
				}).getBody();
		model.addAttribute("targets", targets);

		List<Target> orgTargets = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/targets?orgId=dzFyTqq4dT7YIai8mogz", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Target>>() {
				}).getBody();

		for (Target orgTarget : orgTargets) {
			for (Target target : targets) {
				if (orgTarget.getParentId().equals(target.getId())) {
					orgTarget.setName(target.getName());
					break;
				}
			}
		}
		model.addAttribute("orgTargets", orgTargets);


		return "manageTargets";
	}

	@GetMapping(value = "/integrations")
	public String integrations(Model model, @ModelAttribute("user") User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<Source> sources = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/sources?orgId=" + user.getOrganization().getId() +
				"&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Source>>() {
				}).getBody();
		model.addAttribute("sources", sources);

		List<Target> targets = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/targets", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Target>>() {
				}).getBody();
		model.addAttribute("targets", targets);
		model.addAttribute("integrationsForm", new IntegrationsForm());
		return "manageIntegrations";
	}

	@GetMapping(value = "/consumers")
	public String consumers(@ModelAttribute("user") User user, Model model) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<Consumer> consumers = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/consumers?orgId=" +
				user.getOrganization().getId() + "&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Consumer>>() {
				}).getBody();
		model.addAttribute("consumers", consumers);

		return "manageConsumers";
	}
	

	@GetMapping(value="/manageWorkspaces")
	public ModelAndView manageWorkspaces(@ModelAttribute("user") User user) {
		ModelAndView modelAndView = new ModelAndView();
		System.out.println("User org id == " + user.getOrganization().getId());
		List<Workspace> workspaces = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/workspaces?orgId=" +
						user.getOrganization().getId() , HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Workspace>>() {
				}).getBody();
		modelAndView.addObject("workspaces", workspaces);
		modelAndView.setViewName("manageWorkspaces");
		return modelAndView;
	}
	
	@PostMapping(value = "/createWorkspace")
	public ModelAndView createWorkspace(@ModelAttribute("user") User user, @RequestParam(name="workspace") String workspace) {
		Workspace workspaceObj = new Workspace();
		workspaceObj.setName(workspace);
		workspaceObj.setOrganization(user.getOrganization());
		HttpEntity<Workspace> requestUpdate = new HttpEntity<>(workspaceObj, (HttpHeaders) null);
		ResponseEntity<Void> response = restTemplate.exchange( apiEndPointUri.getDaoApiEndpoint() + "/organizations/" + user.getOrganization().getId() + "/users/" +  user.getId() + "/workspaces", HttpMethod.PUT, requestUpdate , Void.class );
		user.setDefaultWorkspace(workspaceObj);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.toString());
		}
		return changeWorkspaceHelper(user, workspace);
	}
	
	@PostMapping(value = "/changeWorkspace")
	public ModelAndView changeWorkspace(@ModelAttribute("user") User user, @RequestParam(name="workspace") String workspace) {
		return changeWorkspaceHelper(user, workspace);
	}
	
	private ModelAndView changeWorkspaceHelper(User user, String workspace) {
		ModelAndView modelAndView = new ModelAndView();
		
		HttpHeaders headers1 = new HttpHeaders();
		headers1.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("userId",  user.getId().toString());
		map.add("workspace",  workspace);
		System.out.println("workspace == " + workspace);
		/*
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers1);

		ResponseEntity<Workspace> response = restTemplate.postForEntity( apiEndPointUri.getDaoApiEndpoint() + "/organizations/" + user.getOrganization().getId() + "/users/" + user.getId() + "/workspaces", request , Workspace.class );
		//user.setDefaultWorkspace(workspaceO);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody().toString());
		}*/
		modelAndView.setViewName("dashboard");
		return modelAndView;
	}
	
	//@PostMapping(value = "/login")


	/*
	@GetMapping(value = "/eventTester")
	public ModelAndView eventTester(@ModelAttribute("user") User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("eventTester");

		List<EventDefinition> definitions = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/eventDefinitions?orgId=" +
				user.getOrganization().getId() + "&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<EventDefinition>>() {
				}).getBody();

		List<Source> sources = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/sources?orgId=" + user.getOrganization().getId() +
				"&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Source>>() {
				}).getBody();

		for (EventDefinition definition : definitions) {
			for (Source source : sources) {
				if (definition.getSource().getId().longValue() == source.getId().longValue()) {
					definition.setSource(source);
					break;
				}
			}
		}
		modelAndView.addObject("definitions", definitions);
		modelAndView.addObject("sources", sources);

		return modelAndView;
	}*/

	@PostMapping(value = "/validateEventData")
	@ResponseStatus(HttpStatus.OK)
	public void validateEventData(@RequestParam(name="eventId") String eventId, @RequestParam(name="jsonData") String jsonData) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		EventDefinition definition = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/eventDefinition?id=" +
				eventId, HttpMethod.GET, null,
				new ParameterizedTypeReference<EventDefinition>() {
				}).getBody();

		HttpHeaders headers1 = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("jsonSchema",  definition.getSchema());
		map.add("jsonData",  jsonData);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers1);

		ResponseEntity<String> response = restTemplate.postForEntity( apiEndPointUri.getSchemaApiEndpoint() + "/validate", request , String.class );

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}
	}

	@PostMapping(value = "/publishEvent")
	@ResponseStatus(HttpStatus.OK)
	public void publishEvent(@RequestParam(name="jsonData") String jsonData) {
		HttpHeaders headers1 = new HttpHeaders();
		headers1.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonData, headers1);
		ResponseEntity<String> response = restTemplate.postForEntity( apiEndPointUri.getPublisherApiEndpoint() + "/publish", httpEntity , String.class );

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException(response.getBody());
		}
	}
	/*
	@GetMapping(value = "/eventHistory")
	public String eventHistory(Model model, @ModelAttribute("user") User user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<SourceType> orgSourceTypes = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/sourceTypes?orgId=" + user.getOrganization().getId() +
				"&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<SourceType>>() {
				}).getBody();
		model.addAttribute("orgSourceTypes", orgSourceTypes);

		List<Event> events = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/events?orgId=" + user.getOrganization().getId() + "&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Event>>() {
				}).getBody();
		setSourceType(orgSourceTypes, events);
		model.addAttribute("events", events);
		
		List<EventCountsByDay> eventCounts = restTemplate.exchange(apiEndPointUri.getDaoApiEndpoint() + "/organization/eventCountsForPast7Days?orgId=" + user.getOrganization().getId() + "&workspace=" + user.getDefaultWorkspace(), HttpMethod.GET, null,
				new ParameterizedTypeReference<List<EventCountsByDay>>() {
				}).getBody();

		model.addAttribute("eventCounts", eventCounts);
		
		return "eventHistory";
	}


	private void setSourceType(List<SourceType> orgSourceTypes , List<Event> events) {
		for (Event event : events) {
			for (SourceType orgSourceType : orgSourceTypes) {
				if (orgSourceType.getKey().equals(event.getSourceKey())) {
					//System.out.println("inside if");
					event.setSourceName(orgSourceType.getName());
					break;
				}
			}
		}
	}*/
}
