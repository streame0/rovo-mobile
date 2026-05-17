# Native core (rovo-core)

Place `librovo_core.so` per ABI here after building:

```powershell
# From repo root (builds core + copies here and to TV):
.\scripts\build-core-and-sync.ps1
```

Without these libraries, the app falls back to Kotlin HTTP for addon requests.
