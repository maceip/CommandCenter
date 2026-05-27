# Android TV extension (local)

This local fork adds an Android TV focus helper:

- `io.github.alexzhirkevich.cupertino.tv.CupertinoTvFocusable`

It provides:

- D-pad compatible focusability
- Focus scale for depth feedback
- Focus border emphasis compatible with Cupertino rounded surfaces
- Hookable `FocusRequester` to build explicit spatial navigation graphs

This extension is intentionally Android-only (`androidMain`) so the common Cupertino API remains multiplatform-safe.
