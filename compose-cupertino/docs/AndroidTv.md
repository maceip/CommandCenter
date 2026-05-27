# Android TV extension (local)

This local fork adds an Android TV focus helper:

- `io.github.alexzhirkevich.cupertino.tv.CupertinoTvFocusable`
- `io.github.alexzhirkevich.cupertino.tv.rememberCupertinoTvSafeArea`
- `io.github.alexzhirkevich.cupertino.tv.cupertinoTvPerformanceHint`

It provides:

- D-pad compatible focusability
- Focus scale for depth feedback
- Focus border emphasis compatible with Cupertino rounded surfaces
- Hookable `FocusRequester` to build explicit spatial navigation graphs
- Overscan-safe layout defaults for large-screen TV canvases
- A list performance extension point for TV-focused lazy surfaces

This extension is intentionally Android-only (`androidMain`) so the common Cupertino API remains multiplatform-safe.
