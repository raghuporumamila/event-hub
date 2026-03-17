package com.eventhub.site.form;

import com.eventhub.model.Organization;
import com.eventhub.model.User;
import com.eventhub.model.Workspace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationForm {

	private String email;
	private String name;
	private String orgName;
	private String address;
	private String address2;
	private String city;
	private String country;
	private String state;
	private String postalCode;
	
	public Organization getOrg() {
		Organization org = new Organization();
		org.setName(getOrgName());
		org.setAddress(getAddress());
		org.setAddress2(getAddress2());
		org.setCity(getCity());
		org.setCountry(getCountry());
		org.setState(getState());
		org.setPostalCode(getPostalCode());
		return org;
	}
	
	public User getUser(Organization organization, Workspace defaultWorkspace) {
		User user = new User();
		user.setDefaultWorkspace(defaultWorkspace);
		user.setEmail(getEmail());
		user.setName(getName());
		//user.setRole("Admin");
		user.setOrganization(organization);
		return user;
	}
	
}
