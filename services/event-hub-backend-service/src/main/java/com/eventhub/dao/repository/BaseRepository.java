package com.eventhub.dao.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

public class BaseRepository {
    protected Firestore db = FirestoreOptions.getDefaultInstance().getService();;
}