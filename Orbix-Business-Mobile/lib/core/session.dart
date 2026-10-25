import 'package:dio/dio.dart' show HttpClientAdapter;
import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';

import 'accounts.dart';
import 'api/api_client.dart';
import 'api/api_exception.dart';
import 'config/server_config.dart';
import 'json.dart';

/// The saved accounts, the active one, and its API client.
///
/// Every screen reads this. Switching account swaps the client, and the
/// dependents rebuild against the new branch.
class Session extends ChangeNotifier {
  Session._(this._config, this._store, this._accounts, this._activeId,
      this._httpAdapter);

  static Future<Session> create({
    AccountStore? store,
    @visibleForTesting HttpClientAdapter? httpAdapter,
  }) async {
    final s = store ?? AccountStore();
    final config = await ServerConfig.load();
    final accounts = await s.readAll();
    final activeId = await s.readActiveId();
    final session = Session._(config, s, accounts, activeId, httpAdapter);
    session._buildClient();
    return session;
  }

  /// Stands in for the network in tests; null in the app.
  final HttpClientAdapter? _httpAdapter;

  ServerConfig _config;
  final AccountStore _store;
  final List<Account> _accounts;
  String? _activeId;
  ApiClient? _api;

  static const _uuid = Uuid();

  /// The server a new sign-in goes to.
  ServerConfig get config => _config;

  List<Account> get accounts => List.unmodifiable(_accounts);

  Account? get active =>
      _accounts.where((a) => a.id == _activeId).firstOrNull;

  /// True when the active account has a live session.
  bool get signedIn => _api != null && (active?.isSignedIn ?? false);

  /// The account whose session ended and is waiting for its password, if any.
  Account? get needsPassword {
    final a = active;
    return a != null && !a.isSignedIn ? a : null;
  }

  /// Privilege check against the active account's token. Screens gate on this
  /// so a manager without a code sees a clear message, not a refusal.
  bool can(String code) => active?.privileges.contains(code) ?? false;

  bool canAny(Iterable<String> codes) => codes.any(can);

  ApiClient get api {
    final a = _api;
    if (a == null) throw StateError('Not signed in');
    return a;
  }

  void _buildClient() {
    final a = active;
    if (a == null || !a.isSignedIn) {
      _api = null;
      return;
    }
    _api = ApiClient(
      apiBase: a.apiBase,
      accessToken: a.accessToken,
      adapter: _httpAdapter,
    )..onSessionLost = () => _endSession(a.id);
  }

  Future<void> setHost(String host) async {
    _config = ServerConfig(host: ServerConfig.normalise(host));
    await _config.save();
    notifyListeners();
  }

  /// Signs in and makes the account active. The same user on the same server
  /// updates their saved account rather than adding a second one.
  Future<Account> signIn({
    required String host,
    required String username,
    required String password,
  }) async {
    final apiBase = ServerConfig.apiBaseFor(host);
    final token = await ApiClient.login(
      apiBase: apiBase,
      username: username,
      password: password,
    );

    var account = _accounts.where((a) => a.sameLogin(host, username)).firstOrNull;
    if (account == null) {
      account = Account(
        id: _uuid.v4(),
        host: ServerConfig.normalise(host),
        username: username,
      );
      _accounts.add(account);
    }
    account.accessToken = token;
    await _loadProfile(account);

    _activeId = account.id;
    _buildClient();
    await _persist();
    notifyListeners();
    return account;
  }

  /// Name, branch and company for the account list.
  ///
  /// `/users/load_user` blanks the branch and company, so this reads
  /// `/users/get_user`, which returns the user entity. That response carries
  /// more than the app needs — only the three labels are kept, nothing else is
  /// stored. A failure leaves the labels as they were: it must not stop a
  /// manager who has signed in successfully.
  Future<void> _loadProfile(Account account) async {
    try {
      final client =
          ApiClient(apiBase: account.apiBase, accessToken: account.accessToken);
      final user = asMap(
          await client.get('/users/get_user', query: {'username': account.username}));
      final nickname = asStrOr(user['nickname']).trim();
      final first = asStrOr(user['firstName']).trim();
      final last = asStrOr(user['lastName']).trim();
      final fullName = [first, last].where((s) => s.isNotEmpty).join(' ');
      account.displayName = fullName.isNotEmpty ? fullName : nickname;
      final branch = asMap(user['branch']);
      account.branchName = asStrOr(branch['name']).trim();
      final company = asMap(user['company']).isNotEmpty
          ? asMap(user['company'])
          : asMap(branch['company']);
      account.companyName = asStrOr(company['name']).trim();
    } on ApiException {
      // Keep the labels we had.
    }
  }

  /// Makes [account] active. Returns false when its session has ended — the
  /// caller then asks for its password.
  Future<bool> switchTo(Account account) async {
    _activeId = account.id;
    _buildClient();
    await _persist();
    notifyListeners();
    return account.isSignedIn;
  }

  /// Ends the active account's session but keeps the account. Moves to another
  /// signed-in account when there is one.
  Future<void> signOut() async {
    final a = active;
    if (a == null) return;
    _recordLogout(a);
    a.accessToken = '';
    _activeId = _accounts.where((x) => x.isSignedIn).firstOrNull?.id ?? a.id;
    _buildClient();
    await _persist();
    notifyListeners();
  }

  /// Forgets an account entirely.
  Future<void> remove(Account account) async {
    if (account.isSignedIn) _recordLogout(account);
    _accounts.removeWhere((a) => a.id == account.id);
    if (_activeId == account.id) {
      _activeId = (_accounts.where((x) => x.isSignedIn).firstOrNull ??
              _accounts.firstOrNull)
          ?.id;
    }
    _buildClient();
    await _persist();
    notifyListeners();
  }

  /// Leaves the password prompt for a fresh sign-in (another user or server).
  Future<void> clearActive() async {
    _activeId = null;
    _buildClient();
    await _persist();
    notifyListeners();
  }

  /// The server rejected the token: keep the account, drop the session.
  void _endSession(String accountId) {
    final a = _accounts.where((x) => x.id == accountId).firstOrNull;
    if (a == null || a.accessToken.isEmpty) return;
    a.accessToken = '';
    _buildClient();
    _persist();
    notifyListeners();
  }

  /// Rechecks token expiry — on resume, a phone left overnight comes back with
  /// a session that ran out while it slept.
  void recheck() {
    final wasSignedIn = _api != null;
    _buildClient();
    if (wasSignedIn != (_api != null)) notifyListeners();
  }

  /// Tells the server, for its audit log. Fire and forget: a sign-out never
  /// waits on the network.
  void _recordLogout(Account a) {
    ApiClient(apiBase: a.apiBase, accessToken: a.accessToken)
        .post('/logout', body: const {})
        .catchError((_) => null);
  }

  Future<void> _persist() => _store.save(_accounts, _activeId);
}
