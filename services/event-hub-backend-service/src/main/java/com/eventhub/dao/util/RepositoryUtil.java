package com.eventhub.dao.util;

import java.util.UUID;

public class RepositoryUtil {

	public static String getDocumentId() {
		UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return randomUUIDString;
	}
}
