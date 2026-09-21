# Security Policy

## Supported versions

Security fixes go to the latest release and to `main`. Older releases are not supported; update to the latest release.

| Version        | Supported |
| -------------- | --------- |
| `main`         | Yes       |
| Latest release | Yes       |
| Older releases | No        |

## Reporting a vulnerability

**Do not report security vulnerabilities through public issues, discussions or pull requests.**

Report them privately with GitHub private vulnerability reporting:
https://github.com/getfedo/fedo-android-example/security/advisories/new

The maintainers will follow up in the private advisory.

## What to include

- A description of the vulnerability and its impact
- Steps to reproduce or a proof of concept
- Affected files and the commit SHA you tested
- Android version, device or emulator, app version or commit, and the Fedo
  SDK version (`com.getfedo:sdk-android`)

## Never share secrets

Never paste Fedo API keys, the contents of `local.properties`, or any other credentials in issues, pull requests, discussions or advisory reports. Redact them from logcat output and screenshots. If a key was exposed, rotate it in Fedo right away.

A Fedo API key is a client-side key: it is compiled into the APK as
`BuildConfig.FEDO_API_KEY` and ships with the app, so it is not a secret from
anyone holding the binary. Keeping it in the gitignored `local.properties`
stops it from landing in git history and in this public repository — it does
not make it confidential at runtime. Treat it accordingly: rotate it if it
leaks, and never reuse a server-side credential as one.

## Fedo SDK vulnerabilities

This repository only contains the example app. Report vulnerabilities in the Fedo Android SDK (`com.getfedo:sdk-android`) itself to the Fedo team at https://getfedo.com, not here.
