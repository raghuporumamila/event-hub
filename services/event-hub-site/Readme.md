# Event Hub Site - Technical Documentation

## Overview

Event Hub Site is a Spring Boot MVC web application that provides a centralized platform for organizations to manage business events, sources, targets, integrations, and consumer profiles. The application serves as the primary user interface for the Event Hub ecosystem.

## Architecture

### Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Java Version**: 17
- **View Layer**: Thymeleaf templating engine
- **Frontend**: Bootstrap CSS, jQuery, AJAX
- **Security**: Spring Security with BCrypt password encoding
- **Session Management**: Spring Session with Redis
- **Database**: PostgreSQL (AlloyDB Omni compatible)
- **Build Tool**: Maven

### Key Components

```
event-hub-site/
├── src/main/java/com/eventhub/site/
│   ├── Application.java                    # Main Spring Boot application
│   ├── SiteController.java                # Main controller for site operations
│   ├── SiteSecurityController.java        # Authentication & registration
│   ├── ChatController.java                # AI chat integration
│   ├── config/
│   │   ├── ApiEndPointUri.java           # API endpoint configuration
│   │   ├── SecurityConfig.java           # Security configuration
│   │   └── PasswordEncoder.java          # Password encoding utility
│   ├── form/
│   │   ├── RegistrationForm.java         # Registration form model
│   │   └── IntegrationsForm.java         # Integrations form model
│   └── service/
│       └── CustomUserDetailsService.java  # User authentication service
├── src/main/resources/
│   ├── application.yml                    # Main configuration
│   ├── application-local.properties       # Local environment config
│   ├── application-gcp.properties         # GCP environment config
│   ├── templates/                         # Thymeleaf HTML templates
│   └── static/                           # Static resources (CSS, JS, images)
└── pom.xml                               # Maven dependencies
```

## Configuration

### Application Properties

The application supports multiple profiles for different environments:

#### Main Configuration (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eventhub
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: password
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update

evenhub:
  rest:
    client:
      daoApiEndpoint: http://localhost:8081
      schemaApiEndpoint: http://localhost:8082
      publisherApiEndpoint: http://localhost:8083
      genAIAPIEndpoint: http://localhost:8000
```

#### Profile-Specific Configuration

**Local Profile** (`application-local.properties`):
- Direct localhost connections
- Used for local development

**GCP Profile** (`application-gcp.properties`):
- Internal Kubernetes service names
- Redis session management enabled
- Production deployment configuration

### Security Configuration

The application uses Spring Security with form-based authentication:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Role-based access control
        // OrgUser, OrgAdmin, SuperUser roles
        // Public endpoints: /site/security/v1, /css/**, /js/**, etc.
        // Protected endpoints: /site/v1/**
    }
}
```

**Authentication Flow**:
1. User enters credentials
2. CustomUserDetailsService loads user from DAO API
3. BCrypt verifies password
4. Session created and managed by Redis
5. Redirected to dashboard

## Core Features

### 1. Dashboard

**Controller**: `SiteController.dashboard()`
**Template**: `dashboard.html`
**Route**: `/site/v1/dashboard`

Features:
- User profile display
- Workspace information
- Navigation sidebar
- Dynamic content loading area

### 2. Event Management

#### Event Definitions

**Controller**: `SiteController.definitions()`
**Template**: `manageDefinitions.html`
**Route**: `/site/v1/definitions`

Functionality:
- View all event definitions
- Create new event definitions with JSON schemas
- Edit existing definitions
- Delete definitions
- Associate events with sources

**Key Endpoints**:
- `GET /site/v1/definitions` - List all definitions
- `POST /site/v1/createDefinition` - Create new definition

#### Event Testing

**Controller**: `SiteController.eventTester()`
**Template**: `eventTester.html`
**Route**: `/site/v1/eventTester`

Features:
- Test event payloads against schemas
- Validate JSON data
- Publish test events
- Real-time validation feedback

**Key Endpoints**:
- `POST /site/v1/validateEventData` - Validate event against schema
- `POST /site/v1/publishEvent` - Publish event to system

#### Event History

**Controller**: `SiteController.eventHistory()`
**Template**: `eventHistory.html`
**Route**: `/site/v1/eventHistory`

Features:
- View historical events
- Filter by source
- Export capabilities
- Chart visualization (Chart.js)

### 3. Source Management

**Controller**: `SiteController.sources()`
**Template**: `manageSources.html`
**Route**: `/site/v1/sources`

Features:
- List all sources
- Create new sources (REST, KAFKA, WEBHOOK, etc.)
- View source API keys
- Delete sources

**Source Types**:
```java
enum SourceTypeEnum {
    REST,
    KAFKA,
    WEBHOOK,
    PUBSUB,
    // ... etc
}
```

**Key Endpoints**:
- `GET /site/v1/sources` - List sources
- `POST /site/v1/createSource` - Create source
- `POST /site/v1/deleteSource` - Delete source

### 4. Target Management

**Controller**: `SiteController.targets()`
**Template**: `manageTargets.html`
**Route**: `/site/v1/targets`

Features:
- List all targets
- Create new targets with credentials
- Configure target settings
- Delete targets

**Target Types**:
```java
enum TargetTypeEnum {
    KAFKA,
    PUBSUB,
    BIGQUERY,
    // ... etc
}
```

**Key Endpoints**:
- `GET /site/v1/targets` - List targets
- `POST /site/v1/createTarget` - Create target

### 5. Integrations

**Controller**: `SiteController.manageIntegrations()`
**Template**: `manageIntegrations.html`
**Route**: `/site/v1/manageIntegrations`

Features:
- Connect sources to targets
- View existing integrations
- Multi-target selection
- Real-time integration updates

**Workflow**:
1. Select a source
2. Select one or more targets
3. Apply integration
4. System routes events from source to selected targets

**Key Endpoints**:
- `GET /site/v1/manageIntegrations` - Integration UI
- `GET /site/v1/getIntegration?sourceId={id}` - Get integration details
- `POST /site/v1/saveIntegration` - Save integration

### 6. Consumer Management

**Controller**: `SiteController.consumers()`
**Template**: `manageConsumers.html`
**Route**: `/site/v1/consumers`

Features:
- View consumer profiles
- Add new consumers
- Edit consumer information
- Delete consumers

### 7. Workspace Management

**Controller**: `SiteController.manageWorkspaces()`
**Template**: `manageWorkspaces.html`
**Route**: `/site/v1/manageWorkspaces`

Features:
- Create workspaces (DEV, TEST, PROD)
- Switch between workspaces
- Workspace-specific data isolation
- Default workspace setting

**Key Endpoints**:
- `POST /site/v1/createWorkspace` - Create workspace
- `POST /site/v1/changeWorkspace` - Switch workspace

### 8. AI Chat Integration

**Controller**: `ChatController`
**Route**: `/genAIChat`

Features:
- Embedded chat widget
- Real-time AI responses
- Context-aware assistance
- Integration with GenAI API

**Implementation**:
```javascript
// Chat widget in index.html
- Fixed position chat button
- Collapsible chat window
- AJAX communication with backend
- Response streaming
```

## API Integration Pattern

The application follows a consistent pattern for calling backend APIs:

```java
// Example: Getting sources
String url = apiEndPointUri.getDaoApiEndpoint() + 
    "/organizations/" + user.getOrganization().getId() +
    "/workspaces/" + user.getDefaultWorkspace().getId() + 
    "/sources";

List<Source> sources = restTemplate.exchange(
    url, 
    HttpMethod.GET, 
    null,
    new ParameterizedTypeReference<List<Source>>() {}
).getBody();
```

### Backend APIs

The application integrates with three main backend services:

1. **DAO API** (`daoApiEndpoint`): Data access operations
2. **Schema API** (`schemaApiEndpoint`): JSON schema validation
3. **Publisher API** (`publisherApiEndpoint`): Event publishing
4. **GenAI API** (`genAIAPIEndpoint`): AI chat functionality

## Data Models

### User

```java
class User {
    Long id;
    String name;
    String email;
    String password;
    Role role;
    Organization organization;
    Workspace defaultWorkspace;
}
```

### Organization

```java
class Organization {
    Long id;
    String name;
    String address;
    String address2;
    String city;
    String state;
    String country;
    String postalCode;
}
```

### Workspace

```java
class Workspace {
    Long id;
    String name;
    Organization organization;
}
```

### Source

```java
class Source {
    Long id;
    String name;
    SourceTypeEnum type;
    Workspace workspace;
    List<Target> targets;
}
```

### Target

```java
class Target {
    Long id;
    String name;
    TargetTypeEnum type;
    String credentials;
    Workspace workspace;
}
```

### EventDefinition

```java
class EventDefinition {
    Long id;
    String eventName;
    String payloadSchema; // JSON schema
    Source source;
    Organization organization;
    Workspace workspace;
}
```

### Event

```java
class Event {
    Long id;
    String name;
    String payload;
    Timestamp timestamp;
    EventDefinition eventDefinition;
    Source source;
    Organization organization;
    Workspace workspace;
}
```

## Frontend Architecture

### Template Structure

The application uses Thymeleaf fragments for reusability:

**fragments/general.html**:
- `headerfiles`: Common CSS and meta tags
- `siteNavBar`: Navigation bar
- `footer`: Footer section
- `jquery`: jQuery script
- `bootsrtapjs`: Bootstrap JavaScript

**fragments/manageCRUD.html**:
- Shared CRUD table styles
- Modal dialog templates
- Checkbox controls

### AJAX Pattern

Most operations use AJAX to avoid full page reloads:

```javascript
function showSources() {
    $('#contentArea').html("<font color='green'>Loading...</font>");
    $.get('/site/v1/sources', function(data) {
        $('#contentArea').html(data);
    });
}
```

### Dynamic Content Loading

The dashboard uses a content area (`#contentArea`) that dynamically loads different views:

```javascript
// Navigation functions
- showEvents()
- showSources()
- showTargets()
- showIntegrations()
- showConsumers()
- showWorkspaces()
```

## Session Management

### Redis Integration

```java
@Bean
public static ConfigureRedisAction configureRedisAction() {
    return ConfigureRedisAction.NO_OP;
}
```

**Configuration**:
- Store type: Redis
- Session attributes: User object stored in session
- Persistence: Sessions survive application restarts

**Session Attributes**:
```java
@SessionAttributes("user")
public class SiteController {
    // User object automatically managed in session
}
```

## Security Features

### Password Encryption

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Role-Based Access Control

Three user roles:
1. **OrgUser**: Basic access
2. **OrgAdmin**: Administrative access
3. **SuperUser**: Full system access

### CSRF Protection

CSRF protection is disabled for API endpoints:
```java
.csrf(csrf -> csrf.disable())
```

## Deployment

### Local Deployment

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local

# Using Docker
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -t gcr.io/event-hub-249001/event-hub-site:version
```

### GCP/Kubernetes Deployment

```bash
# Using Helm
helm upgrade --install \
  --set image.tag={version} \
  --namespace prod \
  event-hub-site \
  ./helm/blue-green-deploy/prod-galaxy
```

### Docker Image Build

```bash
# Using Jib Maven plugin
mvn compile jib:dockerBuild

# Push to registry
mvn clean compile jib:build
```

## Error Handling

### Common Error Patterns

```java
ResponseEntity<T> response = restTemplate.exchange(...);
if (!response.getStatusCode().equals(HttpStatus.OK)) {
    throw new RuntimeException(response.getBody().toString());
}
```

### Frontend Error Display

```javascript
.fail(function(response) {
    $("#contentArea").html(
        '<font color="red">Error: please try again</font>'
    );
});
```

## Best Practices

### 1. API Calls

- Always use `ParameterizedTypeReference` for generic types
- Check response status before processing
- Include organization and workspace context

### 2. Form Submissions

- Use AJAX for better UX
- Validate data client-side before submission
- Show loading indicators
- Clear modals after successful operations

### 3. Security

- Never store passwords in plain text
- Use session attributes for user context
- Validate user permissions on each request
- Sanitize user inputs

### 4. Code Organization

- Keep controllers focused on routing
- Use service layer for business logic
- Separate concerns (MVC pattern)
- Reuse templates with fragments

## Testing

### Running Tests

```bash
mvn test
```

### Test Structure

```
src/test/java/com/eventhub/site/
└── AppTest.java
```

## Troubleshooting

### Common Issues

**Issue**: Redis connection failed
- **Solution**: Check Redis host/port in application-gcp.properties

**Issue**: API endpoints not accessible
- **Solution**: Verify apiEndPointUri configuration for active profile

**Issue**: Session lost after restart
- **Solution**: Ensure Redis is running and configured

**Issue**: Authentication failing
- **Solution**: Check password encoding and user details service

## Future Enhancements

1. **Enhanced Monitoring**
   - Real-time event dashboards
   - Performance metrics
   - System health indicators

2. **Advanced Integrations**
   - More source and target types
   - Custom transformation rules
   - Conditional routing

3. **Improved UX**
   - React/Vue.js frontend
   - Real-time updates (WebSockets)
   - Mobile responsive design

4. **AI Features**
   - Smart event routing
   - Anomaly detection
   - Predictive analytics

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Thymeleaf Documentation](https://www.thymeleaf.org/)
- [Bootstrap Documentation](https://getbootstrap.com/)
- [Spring Security](https://spring.io/projects/spring-security)

## Support

For issues and questions, refer to the README.md or contact the development team.

## License

Copyright © 2024 Event Hub. All rights reserved.
