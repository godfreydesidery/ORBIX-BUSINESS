import 'dart:convert';

/// An unsigned JWT shaped like the one `CustomAuthenticationFilter` issues:
/// `sub`, `exp` and a `privileges` list. The app never verifies the signature,
/// it only reads the claims.
String fakeToken({
  String sub = 'manager',
  Duration expiresIn = const Duration(hours: 8),
  List<String> privileges = const [],
}) {
  String seg(Map<String, Object> m) =>
      base64Url.encode(utf8.encode(jsonEncode(m))).replaceAll('=', '');
  final exp =
      DateTime.now().toUtc().add(expiresIn).millisecondsSinceEpoch ~/ 1000;
  return '${seg({'alg': 'HS256', 'typ': 'JWT'})}.'
      '${seg({'sub': sub, 'exp': exp, 'privileges': privileges})}.sig';
}
