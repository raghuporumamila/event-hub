variable "project_id" {
    type = string
}
variable "env_prefix" {
    type = string
}
variable "topic_name" {
    type = string
    default = "consumer-events"
}
variable "subscription_name" {
    type = string
    default = "consumer-events_sub"
}

