// Saved accounts: one login per branch, kept across sessions.
//
// The Orbix API takes the branch from the signed-in user, so a manager of
// several branches holds several accounts and switches between them. An
// account must outlive its session — when the token runs out, only the
// password is asked for again.
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:orbix_manager/core/accounts.dart';
import 'package:orbix_manager/core/session.dart';

import 'support/fake_token.dart';

Account _account(
  String id,
  String branch, {
  Duration expiresIn = const Duration(hours: 8),
  List<String> privileges = const [],
  bool signedOut = false,
}) =>
    Account(
      id: id,
      // Unroutable, so the fire-and-forget logout call fails fast.
      host: 'http://127.0.0.1:9',
      username: 'mgr_$id',
      branchName: branch,
      accessToken: signedOut
          ? ''
          : fakeToken(expiresIn: expiresIn, privileges: privileges),
    );

Future<Session> _session(List<Account> accounts, String? activeId) async {
  FlutterSecureStorage.setMockInitialValues({});
  SharedPreferences.setMockInitialValues({});
  await AccountStore().save(accounts, activeId);
  return Session.create();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Account', () {
    test('reads expiry and privileges from the token', () {
      final a = _account('a', 'Dar', privileges: ['LPO-APPROVE', 'FINC-ACCESS']);
      expect(a.isSignedIn, isTrue);
      expect(a.privileges, ['LPO-APPROVE', 'FINC-ACCESS']);
    });

    test('a token that has run out, or is about to, is not a session', () {
      expect(_account('a', 'Dar', expiresIn: const Duration(minutes: -5)).isSignedIn,
          isFalse);
      expect(_account('a', 'Dar', expiresIn: const Duration(seconds: 20)).isSignedIn,
          isFalse);
      expect(_account('a', 'Dar', signedOut: true).isSignedIn, isFalse);
    });

    test('is named after its branch', () {
      expect(_account('a', 'Arusha').title, 'Arusha');
      expect(Account(id: 'x', host: 'h', username: 'jdoe').title, 'jdoe');
    });

    test('the same user on the same server is the same login', () {
      final a = Account(id: 'x', host: 'http://Host:8080/', username: 'JDoe');
      expect(a.sameLogin('http://host:8080/orbix-business-api', 'jdoe'), isTrue);
      expect(a.sameLogin('http://other:8080', 'jdoe'), isFalse);
    });
  });

  group('Session', () {
    test('restores the active account and its privileges', () async {
      final s = await _session([
        _account('a', 'Dar', privileges: ['FINC-ACCESS']),
        _account('b', 'Arusha'),
      ], 'a');
      expect(s.signedIn, isTrue);
      expect(s.active?.title, 'Dar');
      expect(s.can('FINC-ACCESS'), isTrue);
      expect(s.can('LPO-APPROVE'), isFalse);
    });

    test('switching moves to the other branch and is remembered', () async {
      final s = await _session([_account('a', 'Dar'), _account('b', 'Arusha')], 'a');
      expect(await s.switchTo(s.accounts[1]), isTrue);
      expect(s.active?.title, 'Arusha');

      final again = await Session.create();
      expect(again.active?.title, 'Arusha');
    });

    test('switching to a signed-out account asks for its password', () async {
      final s = await _session(
          [_account('a', 'Dar'), _account('b', 'Arusha', signedOut: true)], 'a');
      expect(await s.switchTo(s.accounts[1]), isFalse);
      expect(s.signedIn, isFalse);
      expect(s.needsPassword?.title, 'Arusha');
    });

    test('an expired session keeps the account and asks for the password',
        () async {
      final s = await _session(
          [_account('a', 'Dar', expiresIn: const Duration(minutes: -1))], 'a');
      expect(s.signedIn, isFalse);
      expect(s.needsPassword?.username, 'mgr_a');
      expect(s.accounts, hasLength(1));
    });

    test('signing out keeps the account and moves to a signed-in one',
        () async {
      final s = await _session([_account('a', 'Dar'), _account('b', 'Arusha')], 'a');
      await s.signOut();
      expect(s.accounts, hasLength(2));
      expect(s.accounts.first.isSignedIn, isFalse);
      expect(s.active?.title, 'Arusha');
      expect(s.signedIn, isTrue);
    });

    test('signing out of the only account leaves it waiting for a password',
        () async {
      final s = await _session([_account('a', 'Dar')], 'a');
      await s.signOut();
      expect(s.signedIn, isFalse);
      expect(s.needsPassword?.title, 'Dar');
    });

    test('removing an account forgets it', () async {
      final s = await _session([_account('a', 'Dar'), _account('b', 'Arusha')], 'a');
      await s.remove(s.accounts.first);
      expect(s.accounts.map((a) => a.title), ['Arusha']);
      expect(s.active?.title, 'Arusha');

      final again = await Session.create();
      expect(again.accounts.map((a) => a.title), ['Arusha']);
    });

    test('"use a different account" leaves the password prompt', () async {
      final s = await _session([_account('a', 'Dar', signedOut: true)], 'a');
      await s.clearActive();
      expect(s.needsPassword, isNull);
      expect(s.accounts, hasLength(1));
    });
  });
}
