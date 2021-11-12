# Google Cloud Build MQTT Webhooks

Provides an HTTP endpoint to capture Google Cloud Build push notifications and echo them into an MQTT channel.
The cool kids would probably use a Lambda but we're happy with containers for now.

## Input 

See https://cloud.google.com/cloud-build/docs/send-build-notifications

Specifically we have created a [push subscription](https://cloud.google.com/pubsub/docs/push#configuring-http-endpoints)
to send the build notifications to our endpoint.


## Output

The decoded JSON contents of the push message data field.

The message status string only onto the /statues subtopic.
