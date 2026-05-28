<#macro emailLayout>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <title>KIZUNA</title>
</head>
<body style="margin:0;padding:0;background-color:#050505;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#050505;">
    <tr>
      <td align="center" style="padding:32px 16px;">
        <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="max-width:600px;width:100%;border:1px solid #7f1d1d;border-radius:10px;overflow:hidden;">
          <tr>
            <td style="background:linear-gradient(135deg,#1a0508 0%,#0a0a0c 100%);padding:26px 30px;border-bottom:3px solid #dc2626;">
              <p style="margin:0;font-size:24px;font-weight:800;color:#ffffff;letter-spacing:0.12em;line-height:1;">KIZUNA</p>
              <p style="margin:8px 0 0;font-size:10px;font-weight:700;color:#f87171;letter-spacing:0.32em;text-transform:uppercase;">Industrial Management System</p>
            </td>
          </tr>
          <tr>
            <td style="background-color:#0c0c12;padding:30px 28px;color:#cbd5e1;font-size:15px;line-height:1.65;">
              <#nested>
            </td>
          </tr>
          <tr>
            <td style="background-color:#050508;padding:18px 28px;border-top:1px solid #1e293b;text-align:center;">
              <p style="margin:0;font-size:10px;color:#64748b;letter-spacing:0.14em;text-transform:uppercase;font-weight:700;">Secure notification · Do not forward this link</p>
              <p style="margin:10px 0 0;font-size:9px;color:#475569;">KIZUNA IAM · Automated message</p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>
</#macro>
