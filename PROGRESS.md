# TradeJob Pro - Progreso y Pendientes

## ✅ Completado (02 Junio 2026)

### Arquitectura y Calidad (Fase 2)
- [x] **Estudio de Mercado y Equipo:** Creado `EQUIPO_DESARROLLO.md` con roadmap profesional.
- [x] **Refactorización Core:** `MitoTextField` rediseñado para ser flexible y profesional.
- [x] **UX - Galería Real:** Integración de **Coil** para visualización de fotos en `JobFormScreen`.
- [x] **QA - Unit Testing:** Tests unitarios implementados (`SaveClientUseCaseTest`, `SaveJobUseCaseTest`).
- [x] **CRUD Trabajos Completo:** Eliminación de trabajos con confirmación y filtros por estado.
- [x] **Identidad Visual:** Logo e iconos corregidos en toda la app.

## ✅ Completado (14 Junio 2026)

### Conectividad y Pulido (Fase 3 & 4)
- [x] **Sincronización:** Worker de sincronización programado en `TradeJobApplication`.
- [x] **Gestión de Trabajos:** Campos de presupuesto, importe final y fecha añadidos al flujo.
- [x] **Dashboard:** Pantalla de inicio mejorada con estadísticas reales y trabajos recientes.
- [x] **Shield (QA):** Tests de UI iniciados para el módulo de login.
- [x] **Exportación Avanzada:** Generación de PDF profesional para reportes de trabajo.

## ✅ Completado (26 Junio 2026)

### Micro-Features y Calidad
- [x] **QA - Unit Testing:** Implementados tests unitarios para `JobListViewModel` verificado con `UnconfinedTestDispatcher`.
- [x] **Refactorización Componentes:** `MitoTextField` actualizado con API de `errorText` para validaciones más limpias.
- [x] **Sincronización:** Subida física de fotos a Firebase Storage operativa en `SyncWorker`.

## 🚀 Próximos Pasos (Estrategia de Micro-Features)

> [!IMPORTANT]
> **Nueva Política de Desarrollo:** Cada mejora debe realizarse en una rama de git independiente (`feature/nombre-mejora`), ser pequeña y autocontenida, e incluir (siempre que sea posible) sus respectivos tests. No se permiten cambios grandes directamente en `main` o `develop`.

### 🧪 QA & Testing (Prioridad Actual)
1. **Módulo :home:** Implementar tests unitarios para `JobListViewModel` y `JobFormViewModel`.
2. **Módulo :login:** Ampliar cobertura de tests de UI para `LoginScreen`.

### 🛠️ Refactorizaciones y Mejoras UI
1. **MitoTextField:** Refactorizar para validaciones genéricas (micro-feature en rama propia).
2. **Sync:** Implementar descarga de imágenes desde Cloud Storage si faltan localmente.

## 🚀 Flujo Actual de Navegación

```
LoginScreen → HomeScreen → [Menú] → ClientListScreen → ClientFormScreen
                ↓
         [Cerrar sesión] → LoginScreen
```

## 📝 Notas Técnicas

- Gradle 8.13 configurado
- Java 17 / Kotlin 1.9.24
- Compose BOM 2024.06.00
- Tema: MaterialComponents (Material3 no disponible en versiones actuales)
- Package: `com.tradejob.pro`

---
**Última compilación:** Intentada, errores menores de recursos (iconos)
**Estado:** Funcional lógicamente, pendiente assets visuales
