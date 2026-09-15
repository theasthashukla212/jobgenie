# JobGenie Backend API

Complete backend API for the JobGenie job search platform built with Spring Boot 3.2.0.

## 🚀 Features

- **User Authentication** - JWT-based authentication with login/register endpoints
- **Job Management** - Full CRUD operations for job postings with search and pagination
- **Resume Management** - Users can create and manage multiple resumes
- **Application Tracking** - Track job applications with status updates
- **CORS Enabled** - Configured for frontend development on localhost:5173, 3000, 5174

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **H2 Database** (in-memory for development)
- **PostgreSQL** ready for production
- **Maven** for dependency management

## 📦 Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Node.js 18+ (for frontend)

### 1. Clone the Repository
```bash
git clone https://github.com/theasthashukla212/jobgenie.git
cd jobgenie
```

### 2. Configure Database
The application uses H2 in-memory database by default. For PostgreSQL:

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jobgenie
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 3. Run the Backend
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

### 4. Access H2 Console (Development)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## 🔐 API Endpoints

### Authentication
```
POST /api/auth/register
Body: {
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
Response: { "token": "jwt_token", "email": "...", "firstName": "...", "lastName": "...", "id": 1 }

POST /api/auth/login
Body: {
  "email": "user@example.com",
  "password": "password123"
}
Response: { "token": "jwt_token", "email": "...", "firstName": "...", "lastName": "...", "id": 1 }
```

### Jobs
```
GET /api/jobs?page=0&size=10&search=keyword
GET /api/jobs/{id}
POST /api/jobs
PUT /api/jobs/{id}
DELETE /api/jobs/{id}
```

### Resumes
```
GET /api/resumes?userId=1
GET /api/resumes/{id}
POST /api/resumes
PUT /api/resumes/{id}
DELETE /api/resumes/{id}
```

### Applications
```
GET /api/applications?userId=1
POST /api/applications
PUT /api/applications/{id}
DELETE /api/applications/{id}
```

## 🔗 Frontend Integration

### Connect Frontend to Backend

1. **Update Frontend API Configuration**
   
   In `jobgenie-ui/frontend/src/services/api.js`, ensure the API base URL is set:
   ```javascript
   const API_BASE_URL = 'http://localhost:8080/api';
   ```

2. **Run Frontend**
   ```bash
   cd jobgenie-ui/frontend
   npm install
   npm run dev
   ```

3. **Access Application**
   - Frontend: `http://localhost:5173`
   - Backend API: `http://localhost:8080/api`

### CORS Configuration

CORS is already configured in `SecurityConfig.java` to allow:
- `http://localhost:5173` (Vite default)
- `http://localhost:3000` (React default)
- `http://localhost:5174` (Alternative Vite port)

## 📊 Database Schema

### Users
- id, email, password, firstName, lastName, phone, role, createdAt, updatedAt, enabled

### Jobs
- id, title, company, location, description, type, experience, salary, postedBy, postedAt, updatedAt

### Resumes
- id, user_id, title, content, filePath, isDefault, createdAt, updatedAt

### Applications
- id, user_id, job_id, resume_id, status, notes, appliedAt, updatedAt

## 🧪 Testing

### Test Authentication
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password","firstName":"Test","lastName":"User","phone":"1234567890"}'
```

### Test Jobs API
```bash
curl -X GET http://localhost:8080/api/jobs
```

## 📝 Project Structure

```
src/main/java/com/jobgenie/jobgenie_backend/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   └── JwtAuthenticationFilter.java
├── controller/
│   ├── AuthenticationController.java
│   ├── JobController.java
│   ├── ResumeController.java
│   └── ApplicationController.java
├── model/
│   ├── User.java
│   ├── Job.java
│   ├── Resume.java
│   └── Application.java
├── repository/
│   ├── UserRepository.java
│   ├── JobRepository.java
│   ├── ResumeRepository.java
│   └── ApplicationRepository.java
├── service/
│   └── UserService.java
└── dto/
    ├── AuthRequest.java
    ├── AuthResponse.java
    └── RegisterRequest.java
```

## 🔧 Configuration

### application.properties
```properties
# Server
server.port=8080

# H2 Database (Development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT Configuration
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
app.jwt.expiration=86400000
```

## 🚨 Common Issues

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### CORS Errors
- Ensure frontend is running on allowed origins (5173, 3000, or 5174)
- Check that requests include proper headers

### Database Connection Issues
- For H2: No configuration needed, it's in-memory
- For PostgreSQL: Update application.properties with correct credentials

## 📄 License

MIT License

## 👥 Authors

- Astha Shukla
- Suresh Nagvanshi

---

**Happy Coding! 🎉**
