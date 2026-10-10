# Digital Scholarship Eligibility Portal — 2-hour completion pack

## Stack
- React + Vite frontend (`frontend`)
- Spring Boot + Spring Data JPA backend (`backend`)
- MySQL database `scholarship_portal`

## Before running
1. Back up your existing project.
2. Copy the files from `backend/src/main/java/com/scholarship/backend/` into the same package in your existing backend. Keep your existing `Scholarship.java`, `ScholarshipRepository.java`, and `ScholarshipController.java` CRUD files.
3. Copy `ScholarshipCatalog.java`, `ScholarshipCatalogRepository.java`, and `ScholarshipCatalogController.java` only if you do not already have them. Do not create duplicate classes.
4. Add the new `StudentProfile*`, `ScholarshipApplication*`, and `Eligibility*` files from this pack.
5. Merge the dependencies from `backend/pom.xml` into your existing pom.xml if needed. Do not blindly replace your existing pom.xml.
6. In `application.properties`, keep your own MySQL credentials private. Use:
   `spring.datasource.url=jdbc:mysql://localhost:3306/scholarship_portal`
   `spring.datasource.username=root`
   `spring.datasource.password=YOUR_LOCAL_PASSWORD`
   `spring.jpa.hibernate.ddl-auto=update`
   `server.port=8080`
7. Start Spring Boot, then start the frontend.

## Run frontend
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173

## Run backend
Run `backend` from your existing IDE/Maven project. The pom in this pack is a reference for dependencies and Java 21.

## API endpoints
- Existing applications CRUD: `/api/scholarships`
- Catalogue: `/api/catalog/scholarships`
- Student profiles: `/api/students`
- Submitted applications: `/api/applications`
- Eligibility check: `POST /api/eligibility/check`

## Eligibility note
The checker is a preliminary keyword/rule-based demo using the catalogue's `eligibility` text. It is not an official eligibility decision. Students should confirm current criteria on the provider's official website before applying.
