package com.eventhub.dao.repository.alloydb;

import com.eventhub.model.Source;
import com.eventhub.model.SourceType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceTypeRepository extends ListCrudRepository<SourceType, Long> {
    @Query("SELECT d FROM SourceType d WHERE d.organization.id = :orgId and d.workspace = :workspace")
    List<Source> findByOrgId(@Param("orgId") String orgId, @Param("workspace") String workspace);
}
