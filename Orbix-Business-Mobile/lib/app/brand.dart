/// Which brand this build wears.
///
/// ONE CODEBASE, MANY BRANDS. Orbix Manager ships as a generic Orbix-branded
/// app that works against any customer's server, and as customer-branded apps (their
/// name, icon and colours on the phone). They are the same code: a brand is
/// chosen at build time by an Android flavor plus the matching
/// `brands/<id>/brand.json`, passed with `--dart-define-from-file`. Use
/// `dist/build.ps1 -Brand <id>`; it keeps the two in step.
///
/// A brand changes how the app LOOKS and which server it starts on. It never
/// changes what the app DOES — features follow the signed-in user's
/// permissions on the server, so a branded app and the generic one behave
/// identically against the same server.
///
/// Every value is `const` (`fromEnvironment`), so the theme tokens built from
/// them stay usable in const expressions. The defaults are the Orbix brand, so
/// a plain `flutter run` / `flutter test` with no defines is the generic app.
library;

class Brand {
  Brand._();

  /// `orbix` for the generic app; otherwise the customer key, equal to the
  /// Android flavor name.
  static const String id = String.fromEnvironment(
    'BRAND_ID',
    defaultValue: 'orbix',
  );

  /// The name on the sign-in screen and in the task switcher. The launcher
  /// label comes from the Android flavor (`app_name`), set to the same text.
  static const String appName = String.fromEnvironment(
    'BRAND_APP_NAME',
    defaultValue: 'Orbix Manager',
  );

  static const String tagline = String.fromEnvironment(
    'BRAND_TAGLINE',
    defaultValue: 'Your business, in your pocket.',
  );

  /// The letter on the sign-in tile (the launcher icon carries the same mark).
  static const String mark = String.fromEnvironment(
    'BRAND_MARK',
    defaultValue: 'M',
  );

  /// True for the generic Orbix app. Customer brands say "Powered by Orbix
  /// Manager" so support can still tell what product is on the phone.
  static const bool isOrbix = id == 'orbix';

  // Palette, as 0xAARRGGBB. Defaults are the Orbix green.
  static const int primary = int.fromEnvironment(
    'BRAND_PRIMARY',
    defaultValue: 0xFF13421C,
  );
  static const int primaryDark = int.fromEnvironment(
    'BRAND_PRIMARY_DARK',
    defaultValue: 0xFF0E3215,
  );
  static const int primarySoft = int.fromEnvironment(
    'BRAND_PRIMARY_SOFT',
    defaultValue: 0xFFE8F2EA,
  );

  /// The dark hero field: primary into [heroMid] into [heroEnd].
  static const int heroMid = int.fromEnvironment(
    'BRAND_HERO_MID',
    defaultValue: 0xFF123418,
  );
  static const int heroEnd = int.fromEnvironment(
    'BRAND_HERO_END',
    defaultValue: 0xFF0B1C0F,
  );

  static const int gradientStart = int.fromEnvironment(
    'BRAND_GRADIENT_START',
    defaultValue: 0xFF164D21,
  );
  static const int gradientEnd = int.fromEnvironment(
    'BRAND_GRADIENT_END',
    defaultValue: 0xFF0F3517,
  );

  /// Secondary and tertiary text ON the hero field — tinted towards the brand
  /// so they read as part of it, but light enough to stay legible.
  static const int onDarkSecondary = int.fromEnvironment(
    'BRAND_ON_DARK_2',
    defaultValue: 0xFFBCD1C0,
  );
  static const int onDarkTertiary = int.fromEnvironment(
    'BRAND_ON_DARK_3',
    defaultValue: 0xFF8BA790,
  );
}
