# KIZUNA Keycloak themes

## Login theme
Already configured: `kizuna-theme` (login pages — cyberpunk industrial UI).

## Email theme (new)
Templates live in `kizuna-theme/email/` (dark layout, red accent, English copy).

### Enable in Keycloak
1. Open **http://localhost:8081** → Admin Console.
2. Realm **Kizuna** → **Realm settings** → tab **Themes**.
3. Set **Email theme** to `kizuna-theme`.
4. Save.

### Test email
Realm settings → **Email** → **Test connection** / send test (uses `emailTest` template).

Docker already mounts `./themes` into Keycloak (`docker-compose.yml`). Restart Keycloak after theme file changes:

```bash
docker-compose -p kizuna restart keycloak
```
