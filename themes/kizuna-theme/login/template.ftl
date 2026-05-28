<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}"<#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}"</#if>>

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <script type="importmap">
        {
            "imports": {
                "alpinejs": "${url.resourcesCommonPath}/node_modules/alpinejs/dist/module.esm.js"
            }
        }
    </script>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if authenticationSession??>
        <script type="module">
            import { checkCookiesAndSetTimer } from "${url.resourcesPath}/js/authChecker.js";

            checkCookiesAndSetTimer(
              "${authenticationSession.authSessionId}",
              "${authenticationSession.tabId}",
              "${url.ssoLoginInOtherTabsUrl}"
            );
        </script>
    </#if>
</head>

<body id="keycloak-bg" class="${properties.kcBodyClass!} kizuna-scifi-bg">
  <div class="kizuna-fixed-elements">
    <div class="kizuna-vignette"></div>
    <div class="kizuna-scanlines"></div>
    <div class="kizuna-noise"></div>
    <div class="kizuna-ambient-glow-top"></div>
    <div class="kizuna-glow"></div>
    <div class="kizuna-grid"></div>
  </div>

  <div class="kizuna-main-wrapper" x-data="{open: false}">
    <header class="kizuna-header">
       <div class="kizuna-title-wrapper">
           <div class="kizuna-title-glow"></div>
           <h1 class="kizuna-title">KIZUNA<span class="kizuna-auth-tag">[ACCESS_NODE]</span></h1>
       </div>
       <div class="kizuna-subtitle">SECURE ACCESS GATEWAY // AUTHORIZED PERSONNEL ONLY</div>
    </header>

    <main class="kizuna-terminal-panel">
       <div class="hud-frame-tl"></div>
       <div class="hud-frame-tr"></div>
       <div class="hud-frame-bl"></div>
       <div class="hud-frame-br"></div>

       <div style="margin-bottom: 2rem;">
          <h2 class="kizuna-panel-title"><#nested "header"></h2>
          <p class="kizuna-panel-desc">Provide tactical identification to proceed.</p>
       </div>

        <#-- ALERTS -->
        <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
            <div class="kizuna-alert">
                <div class="pf-v5-c-alert__icon" style="flex-shrink: 0;">
                    <#if message.type = 'error'>[!!! ALERT !!!]</#if>
                    <#if message.type = 'success'>[ SUCCESS ]</#if>
                    <#if message.type = 'warning'>[ WARNING ]</#if>
                    <#if message.type = 'info'>[ INFO ]</#if>
                </div>
                <span>${kcSanitize(message.summary)?no_esc}</span>
            </div>
        </#if>

       <div class="kizuna-form-wrapper">
          <#nested "form">
       </div>

       <div class="hud-separator"></div>

       <#if auth?has_content && auth.showTryAnotherWayLink()>
          <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
              <div class="kizuna-form-options" style="margin-top: 1rem; justify-content: center;">
                  <input type="hidden" name="tryAnotherWay" value="on"/>
                  <a href="#" id="try-another-way"
                      onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">[ ${msg("doTryAnotherWay")} ]</a>
              </div>
          </form>
       </#if>

       <#if displayInfo>
          <div id="kc-info" class="kizuna-form-options" style="margin-top: 0.5rem; justify-content: center;">
              <#nested "info">
          </div>
       </#if>
    </main>

    <footer class="kizuna-footer">
       <div class="kizuna-footer-branding">
          © 2026 KIZUNA DYNAMICS <span style="opacity: 0.3; margin-left: 10px;">v1.0</span>
       </div>
    </footer>
  </div>
<script type="module">
    import Alpine from "alpinejs";

    Alpine.start();
</script>
</body>
</html>
</#macro>