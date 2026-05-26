# Privacy Policy Publish Notes

Use [privacy-policy-en.md](D:\VS code\AndroidWhatsapp\status-vault\docs\privacy-policy-en.md) as the source text for your public privacy policy page.

## Before publishing

Confirmed developer information:

- `Darcy Li`
- `lixiaoyao19950715@gmail.com`

## Recommended publishing path

1. Use [privacy.html](D:\VS code\AndroidWhatsapp\status-vault\docs\privacy.html) as the public web page.
2. Publish it with GitHub Pages using your repository:
   - `https://github.com/cdsyab1995-hash/Statusly`
3. After GitHub Pages is enabled, you will get a public URL similar to:
   - `https://cdsyab1995-hash.github.io/Statusly/privacy.html`
4. Put that final URL into:
   - Google Play Console privacy policy field
   - `PRIVACY_POLICY_URL` in [app/build.gradle.kts](D:\VS code\AndroidWhatsapp\status-vault\app\build.gradle.kts:20)

## Important Google Play note

The privacy policy URL should be:

- publicly accessible;
- not blocked by login;
- readable on mobile devices;
- consistent with your app's actual behavior.
