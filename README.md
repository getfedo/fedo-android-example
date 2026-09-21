# ModelPulse

An Android example app that browses the public [OpenRouter](https://openrouter.ai)
model catalogue and showcases the [Fedo](https://getfedo.com) SDK. It is
documentation for SDK integrators: single module, small, readable.

The full README — screenshots, requirements, quick start and the map of every
Fedo API to the file that uses it — is tracked as its own task and lands next.

## Quick start

```bash
git clone <this repo>
cd fedo-android-example
./gradlew :app:installDebug
```

The app builds and runs without a Fedo API key; the Fedo surfaces then explain
how to add one. To enable them, copy `local.properties.example` and add:

```
FEDO_API_KEY=your-key-here
```

`local.properties` is gitignored.

## License

[MIT](LICENSE) — Copyright (c) 2026 Fedo.

The Fedo SDK (`com.getfedo:sdk-android`) is a separate product, distributed
under its own license terms; this license covers the example app only.
