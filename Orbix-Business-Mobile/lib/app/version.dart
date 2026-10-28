/// The app version shown on the account screen.
///
/// Support asks "what version are you on?" and acts on the answer. Flutter
/// cannot read its own version without a platform plugin, so it is stated
/// once, here.
///
/// **Bump this with `version:` in pubspec.yaml.** They are checked against each
/// other by test/version_test.dart, which fails the build if they drift.
const String kAppVersion = '1.6.1';
