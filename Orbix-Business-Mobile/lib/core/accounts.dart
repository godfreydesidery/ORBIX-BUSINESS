import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'config/server_config.dart';
import 'json.dart';
import 'jwt.dart';

/// One saved sign-in: a user on a server.
///
/// The Orbix API takes the branch from the signed-in user, so a manager who
/// runs several branches has one login per branch — and the app keeps each as
/// an account and switches between them. An account outlives its session:
/// when the token expires the account stays, and signing in again only needs
/// the password.
class Account {
  Account({
    required this.id,
    required this.host,
    required this.username,
    this.displayName = '',
    this.branchName = '',
    this.companyName = '',
    this.accessToken = '',
  });

  /// Stable local id (not the server's user id — two servers can share one).
  final String id;

  /// Scheme+host+port this account signs in to. See [ServerConfig].
  final String host;
  final String username;

  String displayName;
  String branchName;
  String companyName;

  /// Empty when signed out.
  String accessToken;

  /// What to call this account in a list: its branch, else its name.
  String get title => branchName.isNotEmpty
      ? branchName
      : (displayName.isNotEmpty ? displayName : username);

  String get apiBase => ServerConfig.apiBaseFor(host);

  Map<String, dynamic>? get _claims => jwtClaims(accessToken);

  DateTime? get expiresAt {
    final exp = asInt(_claims?['exp']);
    return exp == null
        ? null
        : DateTime.fromMillisecondsSinceEpoch(exp * 1000, isUtc: true);
  }

  /// A session worth using: present and not about to run out mid-approval.
  bool get isSignedIn {
    final exp = expiresAt;
    return accessToken.isNotEmpty &&
        exp != null &&
        exp.isAfter(DateTime.now().toUtc().add(const Duration(minutes: 1)));
  }

  /// Privilege codes from the token — the same list the web app's
  /// `checkPrivilege` reads.
  List<String> get privileges =>
      (_claims?['privileges'] as List?)?.map((e) => e.toString()).toList() ??
      const [];

  /// Same user on the same server: signing in again updates, not duplicates.
  bool sameLogin(String otherHost, String otherUsername) =>
      ServerConfig.normalise(host).toLowerCase() ==
          ServerConfig.normalise(otherHost).toLowerCase() &&
      username.toLowerCase() == otherUsername.toLowerCase();

  Map<String, dynamic> toJson() => {
        'id': id,
        'host': host,
        'username': username,
        'displayName': displayName,
        'branchName': branchName,
        'companyName': companyName,
        'accessToken': accessToken,
      };

  factory Account.fromJson(Map<String, dynamic> j) => Account(
        id: asStrOr(j['id']),
        host: asStrOr(j['host']),
        username: asStrOr(j['username']),
        displayName: asStrOr(j['displayName']),
        branchName: asStrOr(j['branchName']),
        companyName: asStrOr(j['companyName']),
        accessToken: asStrOr(j['accessToken']),
      );
}

/// Saved accounts and which one is active, in the Android Keystore.
class AccountStore {
  AccountStore([FlutterSecureStorage? storage])
      : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  static const _kAccounts = 'mgr.accounts';
  static const _kActive = 'mgr.active_account';

  Future<List<Account>> readAll() async {
    final raw = await _storage.read(key: _kAccounts);
    if (raw == null || raw.isEmpty) return [];
    try {
      return asList(jsonDecode(raw), Account.fromJson)
          .where((a) => a.id.isNotEmpty && a.username.isNotEmpty)
          .toList();
    } on FormatException {
      return [];
    }
  }

  Future<String?> readActiveId() => _storage.read(key: _kActive);

  Future<void> save(List<Account> accounts, String? activeId) async {
    await _storage.write(
      key: _kAccounts,
      value: jsonEncode([for (final a in accounts) a.toJson()]),
    );
    if (activeId == null) {
      await _storage.delete(key: _kActive);
    } else {
      await _storage.write(key: _kActive, value: activeId);
    }
  }
}
