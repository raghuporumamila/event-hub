resource "google_pubsub_topic" "consumer_events_topic" {
    name = "${var.topic_name}_${var.env_prefix}"
}

resource "google_pubsub_subscription" "consumer_events_topic_sub" {
    name  = "${var.subscription_name}_${var.env_prefix}"
    topic = google_pubsub_topic.consumer_events_topic.name

    retain_acked_messages      = true

    ack_deadline_seconds = 20

    retry_policy {
    minimum_backoff = "10s"
    }

    enable_message_ordering    = false
}

terraform {  
    backend "gcs" {
        
    }
}