// Tabs bring themselves up to date.
//
// All four tabs are built at sign-in, so each used to show what it loaded then
// until pulled down: a discount requested after sign-in never appeared on its
// own. Opening the Approvals tab must fetch the queue again — without
// blanking what is already there.
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:orbix_manager/core/accounts.dart';
import 'package:orbix_manager/core/session.dart';
import 'package:orbix_manager/main.dart';

import 'support/fake_server.dart';
import 'support/fake_token.dart';

void main() {
  testWidgets('opening Approvals fetches the queue again', (tester) async {
    tester.view.physicalSize = const Size(412, 915);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);
    SharedPreferences.setMockInitialValues({});
    FlutterSecureStorage.setMockInitialValues({});
    await AccountStore().save([
      Account(
        id: 'a',
        host: 'http://test.local:8080',
        username: 'mgr',
        accessToken: fakeToken(privileges: ['PRCMT-ACCESS']),
      ),
    ], 'a');

    // The second time it is asked, an LPO has arrived.
    var asked = 0;
    final server = FakeServer({
      '/lpos/get_all_pending': (_) => Reply([
            if (asked++ > 0)
              {'id': '9', 'no': 'LPO/9', 'supplierName': 'New Supplier'},
          ]),
      '/grns/get_all_pending': (_) => const Reply(<Object>[]),
    });
    final session = await Session.create(httpAdapter: server);
    await tester.pumpWidget(OrbixManagerApp(session: session));
    await tester.pumpAndSettle();
    expect(asked, 1, reason: 'loaded once at sign-in, for the badge');

    await tester.tap(find.text('Approvals'));
    await tester.pumpAndSettle();
    expect(asked, 2);
    expect(find.text('LPO/9'), findsOneWidget);
    expect(find.text('1'), findsOneWidget, reason: 'the badge follows');
  });
}
