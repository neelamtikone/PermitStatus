# Permit Status Checker

A Java application that monitors Recreation.gov permits and sends SMS notifications when permits become available.

## Features

- Monitors multiple permits simultaneously
- Checks permit availability every 5 minutes
- Sends SMS notifications when permits become available
- Includes detailed availability information in notifications
- Configurable notification cooldown period
- Comprehensive logging

## Prerequisites

- Java 17 or higher
- Maven
- Twilio account (for SMS notifications)

## Setup

1. Clone the repository:
```bash
git clone https://github.com/neelamtikone/PermitStatus.git
cd PermitStatus
```

2. Set up Twilio credentials as environment variables:
```bash
export TWILIO_ACCOUNT_SID="your_account_sid"
export TWILIO_AUTH_TOKEN="your_auth_token"
export TWILIO_PHONE_NUMBER="your_twilio_phone_number"
export RECIPIENT_PHONE_NUMBER="your_phone_number"
```

3. Configure the application:
   - Edit `src/main/resources/config.properties` to customize:
     - Check intervals
     - Notification settings
     - Permit IDs and names
     - Logging settings

4. Build the application:
```bash
mvn clean package
```

## Running the Application

Run the application using:
```bash
java -jar target/permit-status-1.0-SNAPSHOT.jar
```

## Monitoring

- Logs are stored in `logs/permit-checker.log`
- Daily rotating log files are created
- Logs are kept for 30 days by default

## Configuration

The following settings can be modified in `config.properties`:

- `check.interval.minutes`: How often to check for permit availability
- `notification.cooldown.minutes`: Minimum time between notifications
- `api.timeout.seconds`: API request timeout
- `logging.max.history.days`: How long to keep log files

## Adding New Permits

To add a new permit to monitor:

1. Add the permit details to `config.properties`:
```properties
permit.new.id=YOUR_PERMIT_ID
permit.new.name=YOUR_PERMIT_NAME
permit.new.url=YOUR_PERMIT_URL
```

2. Add the permit to the `PERMITS` list in `PermitChecker.java`

## License

MIT License
