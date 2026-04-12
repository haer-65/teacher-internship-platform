# Local Dependency Bundle

This folder contains all runtime dependencies needed to start the project quickly on Windows:

- `apache-maven-3.9.9/`
- `maven-repo/` (offline Maven cache)
- `mysql/mysql-8.0.37-winx64/`
- `redis/`

Runtime files and logs are written outside the repository under `%LOCALAPPDATA%\TeacherInternshipPlatform\` so the project root stays clean during normal start/stop cycles.

## One-command startup

From project root:

```powershell
.\tools\start-project.ps1
```

or

```cmd
.\tools\start-project.cmd
```

It will:

1. Ensure MySQL and Redis are running (service first, local fallback).
2. Start backend with local Maven + local Maven repository.
3. Start frontend dev server.

## One-command stop

```powershell
.\tools\stop-project.ps1
```

or

```cmd
.\tools\stop-project.cmd
```

## Default endpoints

- Frontend: `http://127.0.0.1:5173`
- Backend: `http://127.0.0.1:8080`
- Health check: `http://127.0.0.1:8080/api/v1/health/check`
