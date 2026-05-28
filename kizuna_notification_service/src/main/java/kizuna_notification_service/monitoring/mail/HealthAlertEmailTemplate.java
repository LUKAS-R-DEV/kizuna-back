package kizuna_notification_service.monitoring.mail;

import kizuna_notification_service.monitoring.incident.domain.Incident;
import kizuna_notification_service.monitoring.incident.domain.Status;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * HTML + plain-text templates aligned with KIZUNA cyberpunk UI (#050505, red alerts, mono).
 */
public final class HealthAlertEmailTemplate {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("pt-BR"));

    private HealthAlertEmailTemplate() {
    }

    public static String buildSubject(Incident incident) {
        if (incident.getStatus() == Status.DOWN) {
            return "[KIZUNA] ◆ ALERTA — " + incident.getServiceName() + " OFFLINE";
        }
        return "[KIZUNA] ◇ RESTAURADO — " + incident.getServiceName() + " ONLINE";
    }

    public static String buildPlainText(Incident incident) {
        Theme theme = themeFor(incident.getStatus());
        return """
                ═══════════════════════════════════════
                  KIZUNA // SYSTEM HEALTH MONITOR
                ═══════════════════════════════════════

                %s

                Serviço  : %s
                Status   : %s
                Horário  : %s
                Detalhe  : %s

                ───────────────────────────────────────
                Consulte o painel de Saúde do Sistema.
                KIZUNA Manufacturing Intelligence
                """.formatted(
                theme.bannerPlain(),
                incident.getServiceName(),
                theme.statusLabel(),
                formatTimestamp(incident),
                incident.getMessage()
        );
    }

    public static String buildHtml(Incident incident) {
        Theme theme = themeFor(incident.getStatus());
        String service = escapeHtml(incident.getServiceName());
        String message = escapeHtml(incident.getMessage());
        String timestamp = escapeHtml(formatTimestamp(incident));

        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>KIZUNA Health Alert</title>
                </head>
                <body style="margin:0;padding:0;background-color:#050505;font-family:'Courier New',Courier,monospace;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                         style="background-color:#050505;background-image:linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px),linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px);background-size:40px 40px;">
                    <tr>
                      <td align="center" style="padding:32px 16px;">
                        <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0"
                               style="max-width:600px;width:100%%;background-color:#0a0a0a;border:1px solid %s;border-radius:16px;box-shadow:0 0 40px %s, inset 0 1px 0 rgba(255,255,255,0.06);">
                          <tr>
                            <td style="padding:28px 32px 20px;border-bottom:1px solid rgba(255,255,255,0.06);">
                              <p style="margin:0 0 6px;font-size:10px;letter-spacing:0.35em;color:#64748b;text-transform:uppercase;">
                                SYSTEM HEALTH MONITOR
                              </p>
                              <h1 style="margin:0;font-size:28px;font-weight:700;color:#f8fafc;letter-spacing:0.12em;text-shadow:0 0 20px %s;">
                                KIZUNA
                              </h1>
                              <p style="margin:8px 0 0;font-size:11px;color:#475569;letter-spacing:0.2em;">
                                NEXUS // MANUFACTURING INTELLIGENCE
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px;">
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                     style="background-color:#080808;border:1px solid %s;border-radius:12px;">
                                <tr>
                                  <td style="padding:20px 24px;">
                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                      <tr>
                                        <td style="vertical-align:middle;padding-right:12px;">
                                          <span style="display:inline-block;width:10px;height:10px;border-radius:50%%;background-color:%s;box-shadow:0 0 12px %s;"></span>
                                        </td>
                                        <td style="vertical-align:middle;">
                                          <span style="font-size:11px;font-weight:700;letter-spacing:0.25em;color:%s;text-transform:uppercase;">
                                            %s
                                          </span>
                                        </td>
                                      </tr>
                                    </table>
                                    <p style="margin:16px 0 0;font-size:13px;line-height:1.6;color:#94a3b8;">
                                      %s
                                    </p>
                                  </td>
                                </tr>
                              </table>
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                     style="margin-top:20px;">
                                <tr>
                                  <td style="padding:14px 0;border-bottom:1px solid rgba(255,255,255,0.05);">
                                    <span style="font-size:10px;letter-spacing:0.2em;color:#64748b;text-transform:uppercase;">Serviço</span><br>
                                    <span style="font-size:15px;color:#e2e8f0;font-weight:700;letter-spacing:0.05em;">%s</span>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:14px 0;border-bottom:1px solid rgba(255,255,255,0.05);">
                                    <span style="font-size:10px;letter-spacing:0.2em;color:#64748b;text-transform:uppercase;">Status</span><br>
                                    <span style="font-size:14px;color:%s;font-weight:700;letter-spacing:0.15em;">%s</span>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:14px 0;border-bottom:1px solid rgba(255,255,255,0.05);">
                                    <span style="font-size:10px;letter-spacing:0.2em;color:#64748b;text-transform:uppercase;">Horário</span><br>
                                    <span style="font-size:14px;color:#cbd5e1;">%s</span>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:14px 0 0;">
                                    <span style="font-size:10px;letter-spacing:0.2em;color:#64748b;text-transform:uppercase;">Detalhe</span><br>
                                    <span style="font-size:13px;color:#94a3b8;line-height:1.5;">%s</span>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 32px 28px;border-top:1px solid rgba(255,255,255,0.06);background-color:#070707;border-radius:0 0 16px 16px;">
                              <p style="margin:0;font-size:11px;color:#475569;line-height:1.6;letter-spacing:0.05em;">
                                Acesse o painel <strong style="color:#64748b;">Saúde do Sistema</strong> para histórico de incidentes e status em tempo real.
                              </p>
                              <p style="margin:12px 0 0;font-size:10px;color:#334155;letter-spacing:0.15em;">
                                ◈ KIZUNA — alerta automático · não responda este e-mail
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                theme.borderColor(),
                theme.glowColor(),
                theme.glowColor(),
                theme.borderDim(),
                theme.accentColor(),
                theme.accentColor(),
                theme.accentColor(),
                theme.headlineHtml(),
                theme.summaryHtml(),
                service,
                theme.accentColor(),
                theme.statusLabel(),
                timestamp,
                message
        );
    }

    private static String formatTimestamp(Incident incident) {
        if (incident.getTimestamp() == null) {
            return "—";
        }
        return TIMESTAMP_FMT.format(incident.getTimestamp());
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static Theme themeFor(Status status) {
        if (status == Status.DOWN) {
            return new Theme(
                    "#ef4444",
                    "rgba(239,68,68,0.35)",
                    "rgba(239,68,68,0.25)",
                    "OFFLINE",
                    "◆ ALERTA CRÍTICO",
                    "Serviço indisponível detectado pelo monitor de saúde.",
                    ">>> CRITICAL: SERVICE OFFLINE <<<"
            );
        }
        return new Theme(
                "#10b981",
                "rgba(16,185,129,0.35)",
                "rgba(16,185,129,0.2)",
                "ONLINE",
                "◇ SERVIÇO RESTAURADO",
                "O serviço voltou a responder normalmente.",
                ">>> STATUS: SERVICE RESTORED <<<"
        );
    }

    private record Theme(
            String accentColor,
            String borderColor,
            String glowColor,
            String statusLabel,
            String headlineHtml,
            String summaryHtml,
            String bannerPlain
    ) {
        String borderDim() {
            return borderColor;
        }
    }
}
