# Vocab Daily (MVP)

Minimal Phase 1 scaffold: loads `assets/cefr_words.json` and shows a swipeable word card in Jetpack Compose.

## Open / Run

1. Open the `vocab-daily/` folder in Android Studio.
2. Let Android Studio sync (it will download Gradle + Android deps).
3. Run the `app` configuration on an emulator/device.

## Notes

- Data source: prefers `assets/ENGLISH_CERF_WORDS.csv` (headword + CEFR). Falls back to `assets/cefr_words.json`.
- Swipe: horizontal drag changes the word; tap toggles meaning/extra details.
