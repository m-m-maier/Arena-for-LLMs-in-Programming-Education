#!/bin/bash

# Save selected env vars at runtime to etc/environment (such that cronjob can access them)
printenv | grep -E -w 'DB_HOST|DB_PASSWORD_LLMAPE' > /etc/environment

# Start cron or main process
exec "$@"
