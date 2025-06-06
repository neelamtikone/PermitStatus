#!/bin/bash

# Load environment variables
source ./load_env.sh

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 15 or later."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven."
    exit 1
fi

# Check required environment variables
required_vars=("TWILIO_ACCOUNT_SID" "TWILIO_API_KEY_SID" "TWILIO_API_KEY_SECRET" "TWILIO_PHONE_NUMBER" "NOTIFICATION_PHONE_NUMBER")
missing_vars=()

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "Error: The following environment variables are not set:"
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Please set them using:"
    echo "export TWILIO_ACCOUNT_SID=your_account_sid"
    echo "export TWILIO_API_KEY_SID=your_api_key_sid"
    echo "export TWILIO_API_KEY_SECRET=your_api_key_secret"
    echo "export TWILIO_PHONE_NUMBER=your_twilio_phone_number"
    echo "export NOTIFICATION_PHONE_NUMBER=your_phone_number"
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

# Build the project
echo "Building the project..."
mvn clean package

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Error: Build failed. Please check the errors above."
    exit 1
fi

# Run the application
echo "Starting the application..."
java -jar target/permit-status-1.0-SNAPSHOT.jar 