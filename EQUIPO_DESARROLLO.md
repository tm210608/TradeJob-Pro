# 🛠 TradeJob Pro: Master Development Plan

## 📑 1. Estudio del Proyecto y Mercado
**Objetivo:** Ser la herramienta #1 de gestión de campo para autónomos técnicos en España/LATAM.

### Diferenciadores Clave
1. **Offline-First:** El trabajador no depende de la cobertura en el sótano del cliente.
2. **Evidencia Visual:** Sistema nativo de fotos "Antes/Después" para evitar reclamaciones falsas.
3. **Privacidad:** Datos locales por defecto, sincronización opcional.

---

## 👥 2. Estructura del Equipo (Agentes AI)

| Rol | Agente | Responsabilidad Inmediata |
| :--- | :--- | :--- |
| **Orquestador** | **The Architect** | Gestión de estados, Inyección de dependencias, Roadmap. |
| **UX/UI** | **Pixel** | Refinar `JobFormScreen` para entrada rápida de datos. |
| **Backend** | **Atlas** | Implementar `libs:network` real y Sync Cloud. |
| **QA/Testing** | **Shield** | Cobertura de tests en UseCases y DAOs. |
| **DevOps** | **Orbit** | Configuración de ProGuard y despliegue en Play Store. |

---

## 🚀 3. Hoja de Ruta hasta el Lanzamiento

### Fase 1: Consolidación CRUD (COMPLETADO 🟢)
- [x] Gestión de Clientes.
- [x] Gestión de Trabajos (Básico).
- [x] Iconografía y branding inicial.

### Fase 2: Robustez y UX (EN CURSO 🟡)
- [ ] **Pixel:** Implementar galería de fotos real (Coil) en `JobForm`.
- [ ] **Architect:** Refactorizar `MitoTextField` para soportar validaciones avanzadas.
- [ ] **Shield:** Añadir tests de migración de base de datos.

### Fase 3: Conectividad (PENDIENTE 🔴)
- [ ] **Atlas:** Integración con Firebase Auth y Firestore (Sync).
- [ ] **Atlas:** Exportación de trabajos a PDF/Email.

### Fase 4: Lanzamiento (PENDIENTE 🔴)
- [ ] **Orbit:** Optimización de recursos (R8/ProGuard).
- [ ] **Orbit:** Publicación de versión Beta en Google Play Console.

---
**Nota del Orquestador:** Este documento es la única fuente de verdad para el equipo.
