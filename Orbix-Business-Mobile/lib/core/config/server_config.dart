import 'package:shared_preferences/shared_preferences.dart';

/// Where this phone signs in.
///
/// The Orbix Business API is served under `/orbix-business-api` on every
/// install (the web app's `env.js` uses the same path), so only
/// scheme+host+port is configurable and the app appends the path itself.
/// Typing `.../orbix-business-api` would otherwise double it and every call
/// would 404, so [normalise] strips it.
///
/// This is the server a NEW sign-in goes to. Each saved account remembers the
/// server it was signed in on (see `Account.host`), so changing this never
/// moves an account that already exists.
class ServerConfig {
  ServerConfig({required this.host});

  /// e.g. `http://13.51.26.114:8080`. No trailing slash, no API path.
  String host;

  static const String apiPath = '/orbix-business-api';

  /// Set at build time so a shipped build starts on the right server:
  /// `--dart-define=ORBIX_HOST=http://13.51.26.114:8080` (dist/build.ps1 reads
  /// it from `brands/<id>/brand.json`). The generic Orbix build leaves it
  /// EMPTY — it does not know whose server it will meet — and the sign-in
  /// screen then asks for the address before anything else.
  static const String defaultHost = String.fromEnvironment('ORBIX_HOST');

  static const _kHost = 'mgr.server_host';

  bool get hasHost => trimSlashes(host).isNotEmpty;

  /// Empty while there is no host: Dio rejects a relative base URL, and the app
  /// must still start to ask for the address.
  String get apiBase => apiBaseFor(host);

  static String apiBaseFor(String host) {
    final h = normalise(host);
    return h.isEmpty ? '' : '$h$apiPath';
  }

  static String trimSlashes(String h) {
    var v = h.trim();
    while (v.endsWith('/')) {
      v = v.substring(0, v.length - 1);
    }
    return v;
  }

  /// Strips trailing slashes and a pasted `/orbix-business-api`, so the
  /// double-path trap cannot be typed into the field.
  static String normalise(String input) {
    var v = trimSlashes(input);
    if (v.toLowerCase().endsWith(apiPath)) {
      v = v.substring(0, v.length - apiPath.length);
    }
    return trimSlashes(v);
  }

  /// True for `http://host`, `https://host:port` — what the field must hold.
  static bool looksValid(String input) => RegExp(
        r'^https?://[A-Za-z0-9.\-]+(:\d{1,5})?$',
        caseSensitive: false,
      ).hasMatch(normalise(input));

  static Future<ServerConfig> load() async {
    final p = await SharedPreferences.getInstance();
    return ServerConfig(host: p.getString(_kHost) ?? defaultHost);
  }

  Future<void> save() async {
    final p = await SharedPreferences.getInstance();
    await p.setString(_kHost, normalise(host));
  }
}
