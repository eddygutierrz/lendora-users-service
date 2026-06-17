-- ============================================================
-- users-service · seed inicial de permission_dependency
--
-- Replica las reglas que hasta ahora vivían hardcoded en el
-- frontend (role-edit.page.ts). Idempotente: usa NOT EXISTS para
-- poder correrse varias veces sin duplicar.
--
-- Requisito previo: los permisos referenciados deben existir en
-- la tabla `permissions` (se asume que fueron dados de alta desde
-- la pantalla de admin).
--
-- Uso:
--   psql -h <host> -U users_app -d lendora_users -f seed-permission-dependencies.sql
-- ============================================================

INSERT INTO permission_dependency (permission_id, depends_on_id, kind, created_at)
SELECT p.id, d.id, 'REQUIRES', NOW()
  FROM permissions p, permissions d
 WHERE (p.code, d.code) IN (
     ('customers.create', 'parties.read'),
     ('customers.create', 'parties.create'),
     ('customers.update', 'parties.read'),
     ('customers.update', 'parties.update'),
     ('customers.read',   'parties.read')
   )
   AND NOT EXISTS (
     SELECT 1 FROM permission_dependency x
      WHERE x.permission_id = p.id
        AND x.depends_on_id = d.id
        AND x.kind = 'REQUIRES'
   );

INSERT INTO permission_dependency (permission_id, depends_on_id, kind, created_at)
SELECT p.id, d.id, 'IMPLIES', NOW()
  FROM permissions p, permissions d
 WHERE (p.code, d.code) IN (
     ('customers.read',           'parties.read'),
     ('customers.update',         'parties.read'),
     ('customers.read_prospects', 'parties.read'),
     ('customers.read_customers', 'parties.read')
   )
   AND NOT EXISTS (
     SELECT 1 FROM permission_dependency x
      WHERE x.permission_id = p.id
        AND x.depends_on_id = d.id
        AND x.kind = 'IMPLIES'
   );

-- Verificación rápida
SELECT p.code AS permission, d.code AS depends_on, pd.kind
  FROM permission_dependency pd
  JOIN permissions p ON p.id = pd.permission_id
  JOIN permissions d ON d.id = pd.depends_on_id
 ORDER BY p.code, pd.kind, d.code;
