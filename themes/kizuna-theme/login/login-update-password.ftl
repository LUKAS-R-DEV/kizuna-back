<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>
    <#if section = "header">
        ${msg("updatePasswordTitle")}
    <#elseif section = "form">
        <div id="kc-form">
          <div id="kc-form-wrapper">
            <form id="kc-passwd-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
                <input type="text" id="username" name="username" value="${username}" autocomplete="username"
                       readonly="readonly" style="display:none;"/>
                <input type="password" id="password" name="password" autocomplete="current-password" style="display:none;"/>

                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputGroup!}">
                        <span class="${properties.kcInputClass!}">
                            <input tabindex="1" id="password-new" name="password-new" type="password"
                                   autofocus autocomplete="new-password" placeholder="[ NEW_CRYPTOGRAPHIC_KEY ]"
                                   aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                            />
                        </span>
                    </div>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputGroup!}">
                        <span class="${properties.kcInputClass!}">
                            <input tabindex="2" id="password-confirm" name="password-confirm" type="password"
                                   autocomplete="new-password" placeholder="[ CONFIRM_CRYPTOGRAPHIC_KEY ]"
                                   aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                            />
                        </span>
                    </div>
                    <#if messagesPerField.existsError('password-confirm')>
                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.getFirstError('password-confirm'))?no_esc}
                        </span>
                    </#if>
                    <#if messagesPerField.existsError('password')>
                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.getFirstError('password'))?no_esc}
                        </span>
                    </#if>
                </div>

                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                    <div id="kc-form-options" style="display:flex; justify-content:flex-start; margin: 1.2rem 0; padding-left: 0.5rem;">
                        <#if isAppInitiatedAction??>
                            <label class="pf-v5-c-check" style="display: flex; align-items: center; cursor: pointer;">
                                <input tabindex="3" class="pf-v5-c-check__input" type="checkbox" id="logout-sessions" name="logout-sessions" value="on" checked>
                                <span class="pf-v5-c-check__label" style="font-family:'JetBrains Mono',monospace; font-size:10px; text-transform:uppercase; font-weight:700;">TERMINATE_ACTIVE_SESSIONS</span>
                            </label>
                        <#else>
                            <label class="pf-v5-c-check" style="display: flex; align-items: center; cursor: pointer;">
                                <input tabindex="3" class="pf-v5-c-check__input" type="checkbox" id="logout-sessions" name="logout-sessions" value="on" checked>
                                <span class="pf-v5-c-check__label" style="font-family:'JetBrains Mono',monospace; font-size:10px; text-transform:uppercase; font-weight:700;">TERMINATE_ACTIVE_SESSIONS</span>
                            </label>
                        </#if>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <#if isAppInitiatedAction??>
                        <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" type="submit" value="AUTHORIZE NEW KEY" />
                    <#else>
                        <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="AUTHORIZE NEW KEY" />
                    </#if>
                </div>
            </form>
          </div>
        </div>
    </#if>
</@layout.registrationLayout>
