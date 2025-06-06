#!/bin/bash

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Error: .env file not found"
    echo "Please create a .env file with your Twilio configuration"
    exit 1
fi

# Load environment variables from .env file
export $(cat .env | xargs)

# Verify required variables are set
required_vars=("TWILIO_ACCOUNT_SID" "TWILIO_API_KEY_SID" "TWILIO_API_KEY_SECRET" "TWILIO_PHONE_NUMBER" "NOTIFICATION_PHONE_NUMBER")
missing_vars=()

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "Error: The following variables are not set in .env file:"
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    exit 1
fi

echo "Environment variables loaded successfully" 