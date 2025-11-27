# Deployment

> [!NOTE]
> This branch is focused on deployment configurations. For complete project documentation, architecture details, and setup guides, please switch to the `main` branch.

## Deployment Steps

This repository uses GitHub Actions for automated building and deployment. The workflow is defined in `.github/workflows/deploy.yml`.

### Manual Build Instructions

If you need to build the project manually, follow these steps:

#### Backend
**Prerequisites:** JDK 17, Maven

```bash
# Build all backend services
mvn -B package --file backend/pom.xml
```
The executable JAR files will be generated in the `target` directory of each service module (e.g., `backend/log-collector/target/*.jar`).

#### Frontend
**Prerequisites:** Node.js 22

```bash
cd frontend

# Install dependencies
npm ci

# Build for production
npm run build
```
The static assets will be generated in the `frontend/dist` directory.
