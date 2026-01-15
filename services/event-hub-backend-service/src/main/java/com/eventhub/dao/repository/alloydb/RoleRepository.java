package com.eventhub.dao.repository.alloydb;

import com.eventhub.model.Role;
import org.springframework.data.repository.ListCrudRepository;

public interface RoleRepository extends ListCrudRepository<Role, Long> {
}
