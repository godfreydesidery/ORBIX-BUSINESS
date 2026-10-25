// Reports against a fake Orbix server, with row shapes taken from a copy of
// Sighman's data.
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:orbix_manager/app/app_scope.dart';
import 'package:orbix_manager/app/theme.dart';
import 'package:orbix_manager/core/accounts.dart';
import 'package:orbix_manager/core/session.dart';
import 'package:orbix_manager/features/reports/area_report.dart';
import 'package:orbix_manager/features/reports/best_sellers_report.dart';
import 'package:orbix_manager/features/reports/cashier_report.dart';
import 'package:orbix_manager/features/reports/registrations_report.dart';
import 'package:orbix_manager/features/reports_screen.dart';
import 'package:orbix_manager/models/reports.dart';
import 'package:orbix_manager/widgets/kit.dart';

import 'support/fake_server.dart';
import 'support/fake_token.dart';

final _now = DateTime(2026, 10, 5, 18);

Map<String, Object> _parkingRow(int i) => {
      'cardNo': '',
      'amount': '5000.0',
      'discount': i == 0 ? 1000.0 : 0.0,
      'createdDateTime': '2026-10-05 01:18:41.0',
      'ownerLastName': '',
      'ownerFirstName': 'TO LET',
      'ownerPhoneNo': '',
      'chasisNo': 'NSP130-$i',
      'reason': 'Vehicle and Equipment/Parking',
      'days': 1.0,
      'cashierName': i.isEven ? 'Christian 1' : 'Lydia ',
      // Minutes count down with i, so row 0 is the newest.
      'dateTime':
          '2026-10-05 ${(10 + (5000 - i) ~/ 600).toString().padLeft(2, '0')}:'
          '${((5000 - i) % 60).toString().padLeft(2, '0')}:00.0',
      'payCode': 'CASH',
      'vehicleEquipmentName': 'TOYOTA VITZ',
      'vehicleEquipmentCategory': 'IN-TRANSIT',
    };

final _routes = <String, Reply Function(SeenRequest)>{
  '/finance_reports/get_cash_collections_by_dates': (_) => const Reply([
        {'amount': 300000, 'reason': 'Vehicle and Equipment/Parking', 'payCode': 'CASH', 'cashierName': 'Christian 1'},
        {'amount': 120000, 'reason': 'Goods Storage', 'payCode': 'CASH', 'cashierName': 'Lydia '},
        {'amount': 80000, 'reason': 'Weigh Bridge', 'payCode': 'MPESA', 'cashierName': 'Lydia '},
      ]),
  '/finance_reports/get_parking_detailed_collections_by_dates': (_) =>
      // 90 rows: widget tests cannot decode a response over ~50 KB (Dio moves
      // it to an isolate the test clock never finishes). Month-scale volume —
      // 5,951 rows — was checked against a real server instead.
      Reply([for (var i = 0; i < 90; i++) _parkingRow(i)]),
  '/finance_reports/get_bond_item_detailed_collections_by_dates': (_) =>
      const Reply([
        {
          'amount': 2600169.28,
          'discount': 348000.0,
          'bondItemName': 'MITSUBISHI CANTER',
          'ownerLastName': '-',
          'ownerFirstName': 'MBEKA GROUP',
          'chasisNo': 'FEA50-505278',
          'days': 480.0,
          'bondZoneName': 'Sighman-Temeke Bond',
          'cashierName': 'Amina Myovela',
          'dateTime': '2026-10-05 10:42:23.0',
          'payCode': 'CASH',
        },
      ]),
  '/sales_reports/get_fast_moving_dineables_report_by_dates': (_) =>
      const Reply([
        {'qty': 1029.0, 'amount': '514500', 'dineableName': 'ANDAZI'},
        {'qty': 1372.0, 'amount': '2744000', 'dineableName': 'Nyama Plain'},
      ]),
  '/parking_reports/get_registration_report': (_) => const Reply([
        {'sn': '1', 'chassisNo': 'GP53413958', 'vehicleType': 'HONDA FREED', 'keyStatus': 'YES', 'registeredDate': '2026-10-05T08:00:30', 'registeredBy': 'Richard Daniel'},
        {'sn': '2', 'chassisNo': 'ZZT240-1', 'vehicleType': 'TOYOTA PREMIO', 'keyStatus': 'NO', 'registeredDate': '2026-10-05T09:10:00', 'registeredBy': 'Richard Daniel'},
        {'sn': '3', 'chassisNo': 'NHP10-9', 'vehicleType': 'TOYOTA AQUA', 'keyStatus': 'YES', 'registeredDate': '2026-10-05T07:00:00', 'registeredBy': 'Grace M'},
      ]),
};

Future<FakeServer> _pump(
  WidgetTester tester,
  Widget screen, {
  List<String> privileges = const ['MNGNT-ACCESS'],
  double width = 412,
}) async {
  tester.view.physicalSize = Size(width, 1800);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  SharedPreferences.setMockInitialValues({});
  FlutterSecureStorage.setMockInitialValues({});
  await AccountStore().save([
    Account(
      id: 'a',
      host: 'http://test.local:8080',
      username: 'mgr',
      displayName: 'Zakaria Batista',
      companyName: 'SSH',
      accessToken: fakeToken(privileges: privileges),
    ),
  ], 'a');
  final server = FakeServer(_routes);
  final session = await Session.create(httpAdapter: server);
  await tester.pumpWidget(AppScope(
    session: session,
    child: MaterialApp(theme: buildHqTheme(), home: screen),
  ));
  await tester.pumpAndSettle();
  return server;
}

/// The report's own list, not the period chips that also scroll.
final _reportList = find
    .descendant(
      of: find.byType(CustomScrollView),
      matching: find.byType(Scrollable),
    )
    .first;

void main() {
  group('rows', () {
    test('a parking payment names the vehicle, chassis and days', () {
      final r = CollectionRow.fromJson(CollectionArea.parking, _parkingRow(0));
      expect(r.title, 'TOYOTA VITZ');
      expect(r.detail, 'NSP130-0 · 1 day');
      expect(r.payer, 'TO LET');
      expect(r.cashier, 'Christian 1');
      expect(r.discount, 1000);
      expect(r.when, DateTime(2026, 10, 5, 18, 20));
    });

    test('placeholders like "-" and "NA" are left out', () {
      final r = CollectionRow.fromJson(CollectionArea.workshop, {
        'regNo': 'NA',
        'amount': '10000.0',
        'ownerName': 'NA',
        'machineName': 'HONDA',
        'serviceName': 'Battery Charge',
        'cashierName': 'Patrick Mbwelwa',
        'dateTime': '2026-09-02 17:28:52.0',
      });
      expect(r.title, 'Battery Charge');
      expect(r.detail, 'HONDA');
      expect(r.payer, '');
    });

    test('best sellers read either name field', () {
      expect(BestSeller.fromJson({'productName': 'Oil', 'qty': 2, 'amount': '10'}).name,
          'Oil');
      expect(BestSeller.fromJson({'dineableName': 'Chai', 'qty': 2, 'amount': '10'}).name,
          'Chai');
    });
  });

  testWidgets('the list offers every report to a manager', (tester) async {
    await _pump(tester, const ReportsScreen());
    expect(find.text('By cashier'), findsOneWidget);
    expect(find.text('Parking'), findsOneWidget);
    expect(find.text('Weighbridge'), findsOneWidget);
    expect(find.text('Restaurant best sellers'), findsOneWidget);
    expect(find.text('Vehicle registrations'), findsOneWidget);
  });

  testWidgets('and nothing to a cashier', (tester) async {
    await _pump(tester, const ReportsScreen(), privileges: ['FINC-ACCESS']);
    expect(find.text('You do not have access to this'), findsOneWidget);
  });

  testWidgets('by cashier: one card each, by area', (tester) async {
    final server = await _pump(tester, CashierReport(now: () => _now));
    expect(find.text('TZS 500,000'), findsOneWidget);
    expect(find.text('Christian 1'), findsOneWidget);
    expect(find.text('Lydia'), findsOneWidget);
    expect(find.text('TZS 200,000'), findsOneWidget);
    expect(find.text('Cash TZS 120,000 · M-Pesa TZS 80,000'), findsOneWidget);
    expect(server.seen.single.body, {'from': '2026-10-05', 'to': '2026-10-05'});
  });

  testWidgets('changing the period asks the server again', (tester) async {
    final server = await _pump(tester, CashierReport(now: () => _now));
    await tester.tap(find.descendant(
        of: find.byType(FilterChipsRow), matching: find.text('Yesterday')));
    await tester.pumpAndSettle();
    expect(server.seen.last.body, {'from': '2026-10-04', 'to': '2026-10-04'});
  });

  testWidgets('parking: every payment, newest first, on a narrow phone',
      (tester) async {
    await _pump(
      tester,
      AreaReport(area: CollectionArea.parking, now: () => _now),
      width: 360,
    );
    expect(find.text('TZS 450,000'), findsOneWidget);
    expect(find.text('90 payments · TZS 1,000 in discounts'), findsOneWidget);
    // By cashier, then the newest payment first.
    expect(find.text('BY CASHIER'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('NSP130-0 · 1 day · TO LET'),
      300,
      scrollable: _reportList,
    );
    expect(find.text('− TZS 1,000 discount'), findsOneWidget);
    // The list is built lazily: far rows do not exist yet.
    expect(find.text('NSP130-89 · 1 day · TO LET'), findsNothing);
  });

  testWidgets('bonded items: placeholder owner names are dropped',
      (tester) async {
    await _pump(tester, AreaReport(area: CollectionArea.bond, now: () => _now));
    await tester.scrollUntilVisible(
      find.text('MITSUBISHI CANTER'),
      300,
      scrollable: _reportList,
    );
    expect(
      find.text('FEA50-505278 · Sighman-Temeke Bond · 480 days · MBEKA GROUP'),
      findsOneWidget,
    );
  });

  testWidgets('best sellers are ranked by amount', (tester) async {
    await _pump(tester, BestSellersReport(shop: false, now: () => _now));
    final top = tester.getTopLeft(find.text('Nyama Plain')).dy;
    final next = tester.getTopLeft(find.text('ANDAZI')).dy;
    expect(top, lessThan(next));
    expect(find.text('1,372 · TZS 2,744,000'), findsOneWidget);
  });

  testWidgets('registrations: count, keys, and who registered',
      (tester) async {
    final server =
        await _pump(tester, RegistrationsReport(now: () => _now));
    expect(find.text('3 vehicles'), findsOneWidget);
    expect(find.text('2 handed in with keys'), findsOneWidget);
    expect(find.text('2  67%'), findsOneWidget);
    // Every receptionist: the endpoint wants the nickname, even empty.
    expect(server.seen.single.query, {'nickname': ''});
  });

  testWidgets('the share button offers the report on screen', (tester) async {
    await _pump(tester, CashierReport(now: () => _now));
    await tester.tap(find.byTooltip('Share'));
    await tester.pumpAndSettle();
    // The share sheet carries the report's title.
    expect(find.textContaining('Collections by cashier'), findsNWidgets(2));
  });
}
