# custom-external-service-error


Custom Instrumentation for track external service http call error return code for the following libraries:
- httpclient4.0
- httpurlconnection


## Installation / Usage

1. Drop the extension jar in the newrelic agent's "extensions" folder.
2. Edit the newrelic agent's configuration file (`newrelic.yml`) and add the following properties as applicable to the `common` stanza:

```yaml
# By default,  the following timeslice metrics will be generated.  
#    Custom/External-Returns/<external host>/5xx-Errors
#    Custom/External-Returns/<external host>/4xx-Errors
#    Custom/External-Returns/<external host>/Normal

If customer wants to generate metrics for certain URL, use the following urlwhitelist parameters. wildcard is supported.
  HttpUrlConnection:
    urlwhitelist: /abc,/abc/xyz*,/xyz*

#    Custom/External-Returns/<external host>+<matched urlwhitelist>/5xx-Errors
#    Custom/External-Returns/<external host>+<matched urlwhitelist>/4xx-Errors
#    Custom/External-Returns/<external host>+<matched urlwhitelist>/Normal
#    Custom/External-Returns/<external host>/5xx-Errors
#    Custom/External-Returns/<external host>/4xx-Errors
#    Custom/External-Returns/<external host>/Normal

```

4. Java extensions are typically picked up on-the-fly. If wishing to use that ('hot deploy'), wait a minute or so and then check the logs to see that the extension loaded.
5. If you prefer a cold deploy or it doesn't work right with a hot deploy, restart your JVM after adding the JAR and configurations.
6. Check your results. 

## Troubleshooting

- Set log level to "FINER" in newrelic.yml to capture more detailed info about the extension's attempts. This can be done on-the-fly, and changed back to "INFO" once you have the log entries you need.
