# Funciones añadidas en el fork BlockProt SP26

Comparación realizada entre:

- Original viejo: `D:\A-TODO\CODIGOS\BlockProt-master\BlockProt-master`
- Fork actual: `D:\A-TODO\CODIGOS\BlockProt-master\BlockProt SP26`

Alcance de la comparación:

- Se comparó principalmente `spigot/src/main`, `spigot/build.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `README.md` y recursos del plugin.
- No se considera `RedProtect-master/` como parte funcional de BlockProt SP26; parece ser material externo de referencia.
- No se consideran `build/`, `.gradle/`, `.git/` ni datos generados de servidor.

Resumen técnico del diff principal:

- `spigot/src/main`: 61 archivos modificados o añadidos.
- Aproximadamente `6649` líneas añadidas y `682` eliminadas en código/recursos del plugin.
- El núcleo original de protección NBT de bloques sigue existiendo; el fork añade capas nuevas encima.

## 1. Compatibilidad Java 25, Paper 1.21.x y 26.x

El fork añade soporte explícito para compilar y diagnosticar el plugin en entornos modernos.

Funciones añadidas:

- Compilación con `targetJavaVersion=25`, equivalente a class file `69.0`.
- Actualización del build a Gradle/Shadow compatible con Java 25.
- Configuración de `runServer` para `26.1.2`.
- Detección de esquema de versiones clásico `1.x` y esquema anual `26.x`.
- Diagnóstico de versión de runtime en logs.
- Validación de arranque para:
  - Java mínimo.
  - Disponibilidad de Paper.
  - Soporte de typed inventory views en Paper 1.21.4+.
- Corrección de API moderna Bukkit/Paper:
  - `LeavesDecayEvent` se importa desde `org.bukkit.event.block.LeavesDecayEvent`.
  - `Material` se importa explícitamente en `ClaimEventListener`.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/VersionCompat.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/VersionValidator.java`
- `spigot/build.gradle.kts`
- `gradle.properties`

## 2. Sistema de logs persistentes por sesión

El fork añade logging propio persistente en archivo para diagnósticos.

Funciones añadidas:

- Crea logs por sesión en `plugins/BlockProt/logs/`.
- Cada línea lleva timestamp.
- Registra versión del plugin, versión del servidor y runtime detectado.
- Permite registrar checks de compatibilidad como `PASS`, `FAIL` o `WARN`.
- Se usa en sistemas nuevos como claims, mundos, migración y validación de versión.

Archivo principal:

- `spigot/src/main/java/de/sean/blockprot/bukkit/BlockProtLogger.java`

## 3. Sistema de parcelas 3D, Claims

El cambio más grande del fork es la evolución desde protección individual de bloques hacia parcelas tridimensionales.

Funciones añadidas:

- Modelo de parcela 3D con:
  - UUID interno.
  - Dueño.
  - Nombre visible.
  - Mundo.
  - Coordenadas `minX/minY/minZ` y `maxX/maxY/maxZ`.
  - Volumen calculado.
  - Mensaje de bienvenida opcional.
  - Mapa de flags.
- Persistencia de parcelas en `claims.yml`.
- Carga y guardado asíncrono de claims.
- Detección de solapamientos entre claims.
- Búsqueda de claim por ubicación, ID, nombre y dueño.
- Selección de parcela con varita.
- Flujo de creación:
  - `/bp claim wand`
  - click izquierdo para posición A.
  - click derecho para posición B.
  - partículas de borde.
  - prompt por chat para nombrar la claim.
  - timeout de selección.
- Eliminación de claims.
- Listado de claims del jugador.
- Información detallada de claims.
- Mensajes de bienvenida al entrar a una claim.
- Gestión de welcome message por comando o prompt.

Comandos añadidos bajo `/bp claim`:

- `/bp claim wand`
- `/bp claim delete [name]`
- `/bp claim flags [name]`
- `/bp claim list`
- `/bp claim info [name]`
- `/bp claim welcome [name] [message...]`

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/claims/Claim.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/claims/ClaimFlag.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/claims/ClaimManager.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/claims/ClaimWand.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/commands/ClaimCommand.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/ClaimEventListener.java`

## 4. Flags avanzadas de claims

El fork añade flags de comportamiento para controlar acciones dentro de una parcela.

Flags añadidas:

- `TNT`
- `FIRE`
- `PVP`
- `REDSTONE`
- `FLOW`
- `LEAF_DECAY`
- `MOB_SPAWNING`
- `INVINCIBLE`
- `CHEST`
- `DOOR`
- `LEVER_BUTTON`
- `USE_BUCKET`
- `PET_SPAWNING`
- `PET_RIDING`
- `PET_MOVING`
- `DENY_ENDERPEARL`

Enforcement añadido:

- Bloquear rotura de bloques dentro de claims por no dueños.
- Bloquear colocación de bloques dentro de claims por no dueños.
- Auto-lock de bloques lockeables colocados por el dueño dentro de su claim.
- Bloquear PvP si la flag lo niega.
- Bloquear redstone si la flag lo niega.
- Bloquear fuego si la flag lo niega.
- Filtrar bloques de explosiones si la flag TNT lo niega.
- Bloquear flujo de agua/lava.
- Bloquear decay de hojas.
- Bloquear spawn natural/chunk-gen de mobs.
- Hacer jugadores invencibles dentro de claims si la flag lo permite.
- Bloquear acceso a contenedores no protegidos dentro de claims.
- Bloquear puertas, trampillas y gates.
- Bloquear lever/buttons.
- Bloquear buckets.
- Bloquear tameo, montura y movimiento de animales con lead.
- Bloquear ender pearls.

Importante:

- En el config actual, las funciones avanzadas de claims quedan `enabled: false` por defecto.
- Esto evita que una instalación existente cambie comportamiento sin decisión explícita del admin.

## 5. GUI de administración de claims

El fork añade interfaces de inventario para administrar parcelas.

Funciones añadidas:

- Hub de gestión de claim con Shift + Right Click dentro de una parcela propia.
- GUI para cambiar flags de una claim.
- GUI de confirmación para borrar claims.
- Estado de inventario extendido para recordar claim actual.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/ClaimManagementHub.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/ClaimFlagsInventory.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/ClaimDeleteConfirmInventory.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/InventoryState.java`

## 6. Backend híbrido MySQL + NBT

El fork añade una arquitectura híbrida opcional.

Regla aplicada:

- NBT/Persistent Data Container sigue siendo la fuente de verdad para dueño y amigos de bloques.
- MySQL/MariaDB se usa como índice centralizado para búsquedas rápidas, auditorías y confianza global.

Funciones añadidas:

- Pool de conexiones HikariCP.
- Driver MySQL Connector/J sombreado dentro del JAR.
- Servicio `HybridDatabase`.
- Inicialización opcional mediante `mysql.enabled: false`.
- Creación automática de tablas:
  - `blockprot_global_trust`
  - `blockprot_block_index`
- Índice de bloques protegidos:
  - dueño.
  - mundo.
  - coordenadas.
  - tipo de bloque.
  - timestamp de actualización.
- Índice de confianza global:
  - dueño.
  - amigo.
  - timestamp de actualización.
- Caché local en RAM para confianza global.
- Operaciones SQL programadas con `Bukkit.getScheduler().runTaskAsynchronously()`.
- Actualización del índice al bloquear/desbloquear/limpiar bloques.
- Sincronización de la lista global al añadir o quitar amigos de ajustes de jugador.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/storage/HybridDatabase.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/nbt/BlockNBTHandler.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/nbt/PlayerSettingsHandler.java`
- `spigot/build.gradle.kts`

## 7. Lista maestra de amigos y comando add-all

El fork añade herramientas para gestionar confianza a escala.

Funciones añadidas:

- Nuevo subcomando `/bp friends`.
- Nuevo subcomando `/bp friends addall <player>`.
- `/bp friends` abre la GUI de amigos global/default.
- `/bp friends addall <player>`:
  - busca al jugador online/offline.
  - muestra action bar de búsqueda.
  - recorre los bloques protegidos del ejecutor desde estadísticas.
  - añade el UUID del amigo a todos los bloques propios encontrados.
  - sincroniza el amigo con la lista maestra/default del jugador.
  - reporta cuántos bloques fueron modificados.
- Tab completion para `addall` y jugadores online.

Archivo principal:

- `spigot/src/main/java/de/sean/blockprot/bukkit/commands/FriendsAddAllCommand.java`

## 8. Auditoría SQLite de accesos

El fork añade registro local de auditoría.

Funciones añadidas:

- Base SQLite `blockprot_audit.sqlite`.
- Tabla `audit_log`.
- Índices por:
  - ubicación.
  - jugador.
  - timestamp.
- Registro de eventos:
  - `ACCESS_DENIED`
  - `ACCESS_GRANTED`
- Escrituras asíncronas mediante `CompletableFuture`.
- Consulta de historial por bloque.
- Consulta de historial por jugador.
- Poda automática cuando supera el máximo de entradas.
- GUI de auditoría para mostrar historial de accesos.
- Botón de teletransporte para admins en la GUI de auditoría.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/audit/AuditLogger.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/AuditInventory.java`

## 9. Backup automático y migración segura

El fork añade respaldo preventivo para servidores con datos existentes.

Funciones añadidas:

- Tarea `BackupTask`.
- Backup ZIP asíncrono de `plugins/BlockProt`.
- Carpeta de destino: `plugins/BlockProt/backups/`.
- Rotación de backups con máximo de 10 ZIPs.
- No incluye la carpeta `backups` dentro del ZIP.
- Omite logs de sesión.
- Solo notifica en consola si detecta datos previos.
- Detecta señales de instalación previa:
  - `data.yml`
  - `claims.yml`
  - `blockprot.db`
  - `blockprot_audit.sqlite`
  - `stats.yml`
  - archivos `.yml`, `.db`, `.sqlite`, `.dat` en carpetas legacy.
  - carpetas `DIM-1` y `DIM1`.
- En caso de migración, avisa que el admin debe revisar `config.yml`.
- `ReloadCommand` dispara backup forzado antes de reload.

Archivo principal:

- `spigot/src/main/java/de/sean/blockprot/bukkit/tasks/BackupTask.java`

## 10. Limpieza por inactividad

El fork añade una tarea opcional para liberar protecciones de jugadores inactivos.

Funciones añadidas:

- Configuración `inactivity_cleanup_days`.
- Si el valor es mayor a `0`, corre tarea asíncrona.
- Recorre estadísticas de bloques protegidos.
- Limpia protecciones de jugadores que superen el umbral de inactividad.
- Desactivada por defecto con `-1`.

Archivo principal:

- `spigot/src/main/java/de/sean/blockprot/bukkit/tasks/InactivityCleanupTask.java`

## 11. Configuración granular por mundo

El fork añade `worlds.yml`.

Funciones añadidas:

- Configuración por mundo para:
  - `lockable_tile_entities`
  - `lockable_shulker_boxes`
  - `lockable_blocks`
  - `lockable_doors`
- Mundo con `enabled: false` queda sin protección.
- Mundo con `enabled: true` usa listas propias.
- Mundo no listado usa `config.yml` global.
- Escaneo de mundos cargados al arrancar.
- Población automática no destructiva de mundos faltantes.
- Entradas nuevas se crean con `enabled: false`.
- Reparación básica si `worlds.yml` tiene error YAML.

Archivos principales:

- `spigot/src/main/resources/worlds.yml`
- `spigot/src/main/java/de/sean/blockprot/bukkit/config/WorldsConfig.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/config/DefaultConfig.java`

## 12. Watcher de configuración

El fork añade recarga automática ante cambios de configuración.

Funciones añadidas:

- Tarea periódica para vigilar cambios en archivos de configuración.
- Recarga configuración y traducciones cuando detecta cambios.
- Se inicia en `onEnable`.
- Se detiene en `onDisable`.

Archivo principal:

- `spigot/src/main/java/de/sean/blockprot/bukkit/tasks/ConfigFileWatcher.java`

## 13. Seguridad adicional contra robo no convencional

El fork refuerza varias rutas no convencionales.

Funciones añadidas o endurecidas:

- Bloqueo absoluto opcional de movimiento por pistón para bloques NBT protegidos.
- Configuración:
  - `block_protected_block_piston_movement`
- Protección configurable contra explosiones para bloques NBT protegidos.
- Configuración:
  - `protect_locked_blocks_from_explosions`
- Hopper minecarts ya estaban cubiertos por lógica de hopper; el fork conserva esa protección.
- Auto-lock geográfico en claims para interactuables colocados por el dueño.
- Claims pueden bloquear contenedores no lockeados mediante `CHEST`.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/PistonEventListener.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/ExplodeEventListener.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/HopperEventListener.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/listeners/ClaimEventListener.java`

## 14. Mejoras de inventarios e inputs

El fork añade clases auxiliares de entrada y gestión.

Funciones añadidas:

- `ChatInput` para flujos basados en chat.
- `AnvilInput` como alternativa de input vía GUI.
- Mejoras de estado de inventario para:
  - claim actual.
  - página actual.
  - búsqueda de amigos.
  - auditoría.
- Mejoras en GUI de búsqueda/gestión de amigos.
- Mejoras en redstone settings y lock menu para enlazar nuevas pantallas.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/ChatInput.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/AnvilInput.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/InventoryState.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/BlockLockInventory.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/inventories/RedstoneSettingsInventory.java`

## 15. Ayuda y comandos

El fork añade un comando de ayuda propio.

Funciones añadidas:

- `/bp help`
- `/bp ayuda`
- Lista subcomandos disponibles.
- Tab completion para help.

Archivo principal:

- `spigot/src/main/java/de/sean/blockprot/bukkit/commands/HelpCommand.java`

## 16. Traducciones nuevas

El fork modifica todos los archivos de idioma.

Funciones añadidas:

- Nuevas claves para claims.
- Nuevas claves para flags.
- Nuevas claves para auditoría.
- Nuevas claves para `friends addall`.
- Nuevas claves para mensajes de bienvenida, confirmación y errores.
- Mejora del fallback de traducciones: si faltan claves en disco, se pueden fusionar desde el JAR.

Archivos principales:

- `spigot/src/main/java/de/sean/blockprot/bukkit/TranslationKey.java`
- `spigot/src/main/java/de/sean/blockprot/bukkit/Translator.java`
- `spigot/src/main/resources/lang/*.yml`

## 17. Configuración nueva

Nuevas opciones añadidas a `config.yml`:

```yaml
claims_enabled: false
claims_hub_enabled: false
claims_advanced_enabled: false
claims_inherit_global_trust: false

mysql:
  enabled: false
  host: "127.0.0.1"
  port: 3306
  database: "blockprot"
  username: "blockprot"
  password: ""
  jdbc_url: ""
  pool:
    maximum_pool_size: 10
    minimum_idle: 2
    connection_timeout_ms: 10000

protect_locked_blocks_from_explosions: true
block_protected_block_piston_movement: true

claims_flag_flow_enabled: false
claims_flag_mob_spawning_enabled: false
claims_flag_invincible_enabled: false
claims_flag_deny_enderpearl_enabled: false
claims_flag_pets_enabled: false
claims_flag_chest_enabled: false
claims_flag_door_enabled: false
claims_flag_lever_button_enabled: false
claims_flag_use_bucket_enabled: false

inactivity_cleanup_days: -1
```

Notas:

- Las funciones nuevas de alto impacto quedan apagadas por defecto.
- La protección base original de cofres/bloques lockeables sigue activa.
- El núcleo NBT original no fue reemplazado por MySQL.

## 18. Cambios de dependencias y empaquetado

Dependencias añadidas:

- `com.zaxxer:HikariCP:7.0.2`
- `com.mysql:mysql-connector-j:9.7.0`
- `org.slf4j:slf4j-api` incluido para runtime de Hikari.

Cambios de empaquetado:

- HikariCP se relocaliza a `de.sean.blockprot.bukkit.shaded.hikari`.
- MySQL Connector/J se incluye en el JAR sombreado.
- Se verificó que el JAR final contiene:
  - `de/sean/blockprot/bukkit/shaded/hikari/HikariDataSource.class`
  - `com/mysql/cj/jdbc/Driver.class`
  - `META-INF/services/java.sql.Driver`

## 19. Archivos nuevos principales frente al original

Código nuevo:

- `BlockProtLogger.java`
- `VersionCompat.java`
- `VersionValidator.java`
- `audit/AuditLogger.java`
- `claims/Claim.java`
- `claims/ClaimFlag.java`
- `claims/ClaimManager.java`
- `claims/ClaimWand.java`
- `commands/ClaimCommand.java`
- `commands/FriendsAddAllCommand.java`
- `commands/HelpCommand.java`
- `config/WorldsConfig.java`
- `inventories/AnvilInput.java`
- `inventories/AuditInventory.java`
- `inventories/ChatInput.java`
- `inventories/ClaimDeleteConfirmInventory.java`
- `inventories/ClaimFlagsInventory.java`
- `inventories/ClaimManagementHub.java`
- `listeners/ClaimEventListener.java`
- `storage/HybridDatabase.java`
- `tasks/BackupTask.java`
- `tasks/ConfigFileWatcher.java`
- `tasks/InactivityCleanupTask.java`

Recursos nuevos:

- `spigot/src/main/resources/worlds.yml`
- `LUCKPERMS_SETUP.md`

## 20. Estado de verificación

Comandos ejecutados en el fork SP26:

```powershell
.\gradlew.bat :blockprot-spigot:compileJava
.\gradlew.bat :blockprot-spigot:shadowJar
.\gradlew.bat :blockprot-spigot:build
```

Resultado:

- Compilación Java: correcta.
- Shadow JAR: correcto.
- Build completo: correcto.

JAR generado:

- `spigot/build/libs/blockprot-spigot-1.2.9-main-all.jar`

## 21. Riesgos y puntos pendientes

- `claims_enabled` está en `false` por defecto en el config actual. Si se quiere usar claims, el admin debe activarlo.
- La migración/unificación física de carpetas `DIM-1` y `DIM1` no debe ejecutarse a ciegas. El fork detecta datos legacy para backup, pero no mueve carpetas de mundos automáticamente.
- `plugin.yml` solo declara `/blockprot`; los comandos nuevos son subcomandos internos bajo `/bp`.
- El árbol SP26 contiene `RedProtect-master/`, que no forma parte del plugin BlockProt y conviene excluirlo del release si no se usa como referencia.
