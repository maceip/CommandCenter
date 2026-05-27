Android TV Reference App
========================

This repository contains **ReferenceAppKotlin**, a sample Android TV app that demonstrates how to build a video playback experience optimized for Android TV and Google TV.

## Features

- Leanback templates for browse and playback
- Remote video playback with ExoPlayer
- MediaSession support for playback controls
- Deep linking for Assistant, Cast, and home screen programs
- Home screen channel creation
- Watch Next / continue watching
- Cast Connect receiver support

See [ReferenceAppKotlin/README.md](ReferenceAppKotlin/README.md) for full documentation, including Firebase setup, Cast Connect configuration, and app architecture.

## Getting Started

Clone this repo:

```sh
git clone https://github.com/android/tv-samples.git
cd tv-samples/ReferenceAppKotlin
```

Open the `ReferenceAppKotlin` project in [Android Studio](https://developer.android.com/tools/studio/index.html), or build and run it with the [Android CLI](https://developer.android.com/tools/agents/android-cli):

```sh
cd ReferenceAppKotlin
android describe --project_dir=.
android run --apks=app/build/outputs/apk/debug/app-debug.apk --activity=.MainActivity
```

Then compile and deploy to an Android TV emulator or device.

Need more information about getting started with Android TV? Check the [official getting started guide](https://developer.android.com/training/tv/start/start.html).

## Support

If you need additional help, our community might be able to help.

- Stack Overflow: [http://stackoverflow.com/questions/tagged/android-tv](http://stackoverflow.com/questions/tagged/android-tv)

## License

See the [LICENSE file](LICENSE) for details.
