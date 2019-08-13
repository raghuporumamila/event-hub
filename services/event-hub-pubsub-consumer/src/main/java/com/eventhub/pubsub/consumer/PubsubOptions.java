package com.eventhub.pubsub.consumer;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.ValueProvider;

public interface PubsubOptions extends DataflowPipelineOptions {
	
	ValueProvider<String> getSubscriptionName();
	void setSubscriptionName(ValueProvider<String> name);
}
