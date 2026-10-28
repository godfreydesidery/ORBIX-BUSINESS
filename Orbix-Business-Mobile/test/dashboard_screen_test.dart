// The dashboard against a fake Orbix server.
//
// Checks what it asks the server for (the date ranges), what it shows, that a
// running day is never shown as a fall, that a supporting figure failing does
// not take the screen down, and that a cashier's login sees no figures.
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:orbix_manager/app/app_scope.dart';
import 'package:orbix_manager/app/theme.dart';
import 'package:orbix_manager/core/accounts.dart';
import 'package:orbix_manager/core/session.dart';
import 'package:orbix_manager/features/dashboard_screen.dart';
import 'package:orbix_manager/widgets/common.dart';
import 'package:orbix_manager/widgets/kit.dart';

import 'support/fake_server.dart';
import 'support/fake_token.dart';

final _now = DateTime(2026, 10, 25, 14, 30);

/// Collections by date: today 450,000 so far; the 24th 1,000,000; the 23rd
/// 800,000.
Reply _collections(SeenRequest r) {
  final from = (r.body as Map)['from'];
  return Reply(switch (from) {
    '2026-10-25' => [
      {
        'reason': 'Vehicle and Equipment/Parking',
        'amount': 300000,
        'payCode': 'CASH',
        'cashierName': 'Asha',
      },
      {
        'reason': 'Goods Storage',
        'amount': 150000,
        'payCode': 'MPESA',
        'cashierName': 'Juma',
      },
    ],
    '2026-10-24' => [
      {
        'reason': 'Vehicle and Equipment/Parking',
        'amount': 1000000,
        'payCode': 'CASH',
        'cashierName': 'Asha',
      },
    ],
    '2026-10-23' => [
      {
        'reason': 'Bond',
        'amount': 800000,
        'payCode': 'BANK',
        'cashierName': 'Juma',
      },
    ],
    _ => <Object>[],
  });
}

Map<String, Reply Function(SeenRequest)> _routes({bool yardFails = false}) => {
  '/finance_reports/get_cash_collections_by_dates': _collections,
  '/parking_reports/get_totals_by_dates': (_) => yardFails
      ? const Reply('Invalid operation: broken', status: 409)
      : const Reply({
          'registered': '12',
          'paid': '9',
          'checkedOut': '7',
          'currentUnpaid': '41',
          'currentTotalInYards': '41',
        }),
  '/storage_reports/get_totals_by_dates': (_) =>
      const Reply({'checkedIn': '18'}),
  '/bond_item_reports/get_totals_by_dates': (_) =>
      const Reply({'checkedIn': '0'}),
  '/parkings/get_parking_summary': (_) => const Reply([
    {'month': 9, 'checkedIn': 120, 'checkedOut': 100},
    {'month': 10, 'checkedIn': 95, 'checkedOut': 80},
  ]),
  '/storages/get_storage_summary': (_) => const Reply(<Object>[]),
  '/bond_items/get_bond_item_summary': (_) => const Reply(<Object>[]),
  '/days/get_bussiness_date': (_) =>
      const Reply({'bussinessDate': '2026-10-25'}),
};

Future<(Session, FakeServer)> _pump(
  WidgetTester tester, {
  List<String> privileges = const ['MNGNT-ACCESS'],
  bool yardFails = false,
  double width = 412,
}) async {
  tester.view.physicalSize = Size(width, 2400);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  SharedPreferences.setMockInitialValues({});
  FlutterSecureStorage.setMockInitialValues({});
  final account = Account(
    id: 'a',
    host: 'http://test.local:8080',
    username: 'mgr',
    branchName: 'Dar es Salaam',
    companyName: 'Sighman Safari Haulage',
    accessToken: fakeToken(privileges: privileges),
  );
  await AccountStore().save([account], 'a');
  final server = FakeServer(_routes(yardFails: yardFails));
  final session = await Session.create(httpAdapter: server);
  await tester.pumpWidget(
    AppScope(
      session: session,
      child: MaterialApp(
        theme: buildHqTheme(),
        home: DashboardScreen(now: () => _now),
      ),
    ),
  );
  await tester.pumpAndSettle();
  return (session, server);
}

void main() {
  testWidgets('today: the total so far, never shown as a fall', (tester) async {
    final (_, server) = await _pump(tester);

    expect(find.text('How much did we collect today?'), findsOneWidget);
    expect(find.text('TZS 450,000'), findsOneWidget);
    expect(
      find.text('Collected so far today, mostly from parking.'),
      findsOneWidget,
    );
    expect(find.text('SO FAR'), findsOneWidget);
    // Yesterday is shown for reference, without an arrow.
    expect(
      find.descendant(
        of: find.byType(HeroCard),
        matching: find.text('Yesterday'),
      ),
      findsOneWidget,
    );
    expect(find.text('TZS 1,000,000'), findsOneWidget);
    expect(find.byIcon(Icons.arrow_drop_down), findsNothing);

    final dates = server.seen
        .where(
          (r) => r.path == '/finance_reports/get_cash_collections_by_dates',
        )
        .map((r) => r.body)
        .toList();
    expect(
      dates,
      containsAll([
        {'from': '2026-10-25', 'to': '2026-10-25'},
        {'from': '2026-10-24', 'to': '2026-10-24'},
      ]),
    );
  });

  testWidgets('today: where it came from, how it was paid, who took it', (
    tester,
  ) async {
    await _pump(tester);

    expect(find.text('Parking'), findsOneWidget);
    expect(find.text('Storage'), findsOneWidget);
    expect(find.text('TZS 300,000  67%'), findsWidgets);
    expect(find.text('Cash'), findsWidgets);
    expect(find.text('M-Pesa'), findsWidgets);
    expect(find.text('Asha'), findsOneWidget);
    expect(find.text('Juma'), findsOneWidget);
    expect(find.text('2 CASHIERS'), findsOneWidget);
  });

  testWidgets('on site now, and this year by month', (tester) async {
    await _pump(tester);

    expect(find.text('41'), findsOneWidget);
    expect(find.text('12 in · 7 out today'), findsOneWidget);
    expect(find.text('18'), findsOneWidget);
    // No bonded items on site, and none this year: no empty tile or chart.
    expect(find.text('BONDED ITEMS'), findsNothing);
    expect(find.text('IN AND OUT'), findsOneWidget);
    // The year chip, on the current year.
    expect(find.text('2026'), findsOneWidget);
    // Both lines, each with its total for the year.
    expect(find.text('Checked in'), findsOneWidget);
    expect(find.text('215'), findsOneWidget);
    expect(find.text('Checked out'), findsOneWidget);
    expect(find.text('180'), findsOneWidget);
  });

  testWidgets('a finished day is compared, with the change', (tester) async {
    await _pump(tester);
    await tester.tap(
      find.descendant(
        of: find.byType(FilterChipsRow),
        matching: find.text('Yesterday'),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('How much did we collect yesterday?'), findsOneWidget);
    expect(
      find.descendant(
        of: find.byType(HeroCard),
        matching: find.text('TZS 1,000,000'),
      ),
      findsOneWidget,
    );
    expect(
      find.text('Up 25% on the day before, mostly from parking.'),
      findsOneWidget,
    );
    expect(find.text('COMPLETE'), findsOneWidget);
    expect(find.byIcon(Icons.arrow_drop_up), findsOneWidget);
  });

  testWidgets('a failing supporting figure leaves the collections up', (
    tester,
  ) async {
    await _pump(tester, yardFails: true);

    expect(find.text('TZS 450,000'), findsOneWidget);
    expect(find.text('ON SITE NOW'), findsNothing);
    expect(
      find.text('Some figures could not be loaded. Pull down to try again.'),
      findsOneWidget,
    );
  });

  testWidgets('shows the branch and the business day', (tester) async {
    await _pump(tester);
    expect(find.text('Good afternoon, mgr'), findsOneWidget);
    expect(find.text('Dar es Salaam'), findsOneWidget);
    expect(
      find.text('Sighman Safari Haulage · Business day 25 Oct 2026'),
      findsOneWidget,
    );
  });

  testWidgets('a login without management access sees no figures', (
    tester,
  ) async {
    final (_, server) = await _pump(tester, privileges: ['FINC-ACCESS']);

    expect(find.text('You do not have access to this'), findsOneWidget);
    expect(
      server.seen.where(
        (r) => r.path == '/finance_reports/get_cash_collections_by_dates',
      ),
      isEmpty,
    );
  });

  testWidgets('fits a 360-wide phone', (tester) async {
    // A layout overflow fails the test on its own.
    await _pump(tester, width: 360);
    // The chips scroll sideways on a narrow phone.
    await tester.drag(find.byType(FilterChipsRow), const Offset(-300, 0));
    await tester.pumpAndSettle();
    await tester.tap(
      find.descendant(
        of: find.byType(FilterChipsRow),
        matching: find.text('This month'),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('How much did we collect this month?'), findsOneWidget);
  });

  testWidgets('another year is fetched when picked', (tester) async {
    final (_, server) = await _pump(tester);
    bool summary(SeenRequest r) => r.path == '/parkings/get_parking_summary';
    expect(server.seen.where(summary).map((r) => r.query), [
      {'year': 2026},
    ]);

    await tester.tap(find.text('2026'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('2025').last);
    await tester.pumpAndSettle();

    expect(server.seen.where(summary).last.query, {'year': 2025});
    expect(find.text('2025'), findsOneWidget);
    // A finished year runs to December.
    expect(find.text('Checked in'), findsOneWidget);
  });
}
