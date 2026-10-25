// The sign-in screen.
//
// * The server address is baked into a branded build and hidden: a manager
//   never needs it, and a phone pointed at the wrong server looks exactly like
//   a broken app. Support reaches it with seven taps on the footer.
// * A build with no server (the generic Orbix app) asks for it openly first.
// * An account whose session ended asks only for its password.
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:orbix_manager/app/app_scope.dart';
import 'package:orbix_manager/app/theme.dart';
import 'package:orbix_manager/core/accounts.dart';
import 'package:orbix_manager/core/session.dart';
import 'package:orbix_manager/features/sign_in_screen.dart';

const _footer = 'Protected by your company\'s server';
const _host = 'http://13.51.26.114:8080';

Future<Session> _pump(
  WidgetTester tester, {
  String host = _host,
  List<Account> accounts = const [],
  Account? reauth,
}) async {
  tester.view.physicalSize = const Size(412, 915);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  SharedPreferences.setMockInitialValues({'mgr.server_host': host});
  FlutterSecureStorage.setMockInitialValues({});
  await AccountStore().save(accounts, reauth?.id);
  final session = await Session.create();
  await tester.pumpWidget(AppScope(
    session: session,
    child: MaterialApp(
      theme: buildHqTheme(),
      home: Builder(
        builder: (context) => SignInScreen(
          account: AppScope.of(context).session.needsPassword,
        ),
      ),
    ),
  ));
  await tester.pump();
  return session;
}

Future<void> _tapFooter(WidgetTester tester, int times) async {
  for (var i = 0; i < times; i++) {
    await tester.tap(find.text(_footer));
    await tester.pump();
  }
}

void main() {
  testWidgets('the address is nowhere on the screen', (tester) async {
    await _pump(tester);
    expect(find.textContaining('13.51.26.114'), findsNothing);
    expect(find.text('Server address'), findsNothing);
    expect(find.text('Username'), findsOneWidget);
  });

  testWidgets('a few taps do nothing visible', (tester) async {
    await _pump(tester);
    await _tapFooter(tester, 3);
    expect(find.text('Server address'), findsNothing);
    expect(find.text('4 more'), findsNothing);
  });

  testWidgets('seven taps open the address, and it can be changed',
      (tester) async {
    final session = await _pump(tester);
    await _tapFooter(tester, 5);
    expect(find.text('2 more'), findsOneWidget);
    await _tapFooter(tester, 2);
    await tester.pumpAndSettle();

    expect(find.text('Server address'), findsOneWidget);
    // Shown as text for support to read back, and pre-filled to edit.
    expect(find.text(_host), findsWidgets);

    await tester.enterText(
        find.byType(TextField).last, 'http://10.0.0.5:8080/orbix-business-api/');
    await tester.tap(find.text('Save'));
    await tester.pumpAndSettle();
    expect(session.config.host, 'http://10.0.0.5:8080');
  });

  testWidgets('a malformed address is refused', (tester) async {
    final session = await _pump(tester);
    await _tapFooter(tester, 7);
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField).last, '10.0.0.5');
    await tester.tap(find.text('Save'));
    await tester.pumpAndSettle();
    expect(find.textContaining('Use http://address'), findsOneWidget);
    expect(session.config.host, _host);
  });

  testWidgets('with no server, it asks for one before anything else',
      (tester) async {
    await _pump(tester, host: '');
    expect(find.text("Connect to your company's server"), findsOneWidget);
    expect(find.text('Username'), findsNothing);

    await tester.tap(find.text('Enter server address'));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField).last, _host);
    await tester.tap(find.text('Save'));
    await tester.pumpAndSettle();
    expect(find.text('Username'), findsOneWidget);
  });

  testWidgets('an ended session asks only for the password', (tester) async {
    final expired = Account(
      id: 'a',
      host: _host,
      username: 'jdoe',
      branchName: 'Arusha',
    );
    await _pump(tester, accounts: [expired], reauth: expired);

    expect(find.text('Your session has ended'), findsOneWidget);
    expect(find.textContaining('Arusha'), findsOneWidget);
    final username = tester.widget<TextField>(find.byType(TextField).first);
    expect(username.controller?.text, 'jdoe');
    expect(username.readOnly, isTrue);
    expect(find.text('Use a different account'), findsOneWidget);

    // The address stays hidden here too: this account keeps its own server.
    await _tapFooter(tester, 7);
    await tester.pumpAndSettle();
    expect(find.text('Server address'), findsNothing);
  });

  testWidgets('saved accounts are offered under a fresh sign-in',
      (tester) async {
    await _pump(tester, accounts: [
      Account(id: 'a', host: _host, username: 'dar_mgr', branchName: 'Dar'),
      Account(id: 'b', host: _host, username: 'aru_mgr', branchName: 'Arusha'),
    ]);
    expect(find.text('OR CONTINUE AS'), findsOneWidget);
    expect(find.text('Dar'), findsOneWidget);
    expect(find.text('Arusha'), findsOneWidget);
  });

  testWidgets('empty fields are caught before the network', (tester) async {
    await _pump(tester);
    await tester.tap(find.text('Sign in'));
    await tester.pump();
    expect(find.text('Enter your username and password.'), findsOneWidget);
  });
}
