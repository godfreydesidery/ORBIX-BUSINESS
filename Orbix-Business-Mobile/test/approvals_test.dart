// Approvals against a fake Orbix server.
//
// What is listed, what each decision sends, what happens when the server
// refuses, and who sees which section. The shapes are the real ones, taken
// from a copy of Sighman's data.
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:orbix_manager/app/app_scope.dart';
import 'package:orbix_manager/app/theme.dart';
import 'package:orbix_manager/core/accounts.dart';
import 'package:orbix_manager/core/session.dart';
import 'package:orbix_manager/features/approvals_screen.dart';
import 'package:orbix_manager/models/approvals.dart';

import 'support/fake_server.dart';
import 'support/fake_token.dart';

const _parking = {
  'id': '22',
  'no': 'PKN/22',
  'ownerFirstName': 'DULLAH',
  'ownerMiddleName': '',
  'ownerLastName': '',
  'ownerCompanyName': '',
  'agentName': 'GRAND MOVERS',
  'chasisNo': 'VPE25-035926',
  'registrationNo': null,
  'cardNo': '660',
  'status': 'CHECKED-IN',
  'vehicleEquipmentTypeName': 'NISSAN CARAVAN',
};

const _requests = [
  {
    'id': '1901',
    'serviceBillId': '',
    'serviceBillName': 'Parking',
    'billAmount': '3010000.0',
    'discountAmount': '500000.0',
    'status': 'PENDING',
    'created': '',
    'approved': '',
    'branch': '',
    'description': 'Parking Bill 2025-02-11T00:00 2026-10-06T00:00',
    'reason': 'Long-standing customer',
    'comments': null,
  },
  // Decided in the last 48 hours: the endpoint returns it, the app must not.
  {
    'id': '1880',
    'serviceBillName': 'Parking',
    'billAmount': '10000.0',
    'discountAmount': '2000.0',
    'status': 'APPROVED',
    'reason': 'old',
  },
];

const _lpo = {
  'id': '7',
  'no': 'LPO/7',
  'orderDate': '2026-10-04',
  'validUntilDate': '2026-10-30',
  'status': 'PENDING',
  'shopName': 'Main Shop',
  'supplierName': 'Kilimanjaro Supplies',
  'lpoDetails': [
    {
      'productName': 'Engine oil 5L',
      'productCode': 'P-001',
      'baseUom': 'PCS',
      'qty': '10',
      'costPriceVatIncl': '45000',
      'amount': '450000',
    },
    {
      'productName': 'Brake fluid',
      'productCode': 'P-002',
      'baseUom': 'PCS',
      'qty': '4',
      'costPriceVatIncl': '12500',
      'amount': '',
    },
  ],
};

Map<String, Reply Function(SeenRequest)> _routes({
  Reply? approveReply,
  List<Object> lpos = const [_lpo],
}) =>
    {
      '/parkings/get_all_with_discounts_page': (_) => const Reply({
            'content': [_parking],
            'totalElements': 1,
          }),
      '/storages/get_all_with_discounts_page': (_) =>
          const Reply({'content': <Object>[], 'totalElements': 0}),
      '/bond_items/get_all_with_discounts_page': (_) =>
          const Reply({'content': <Object>[], 'totalElements': 0}),
      '/discount_requests': (_) => const Reply(_requests),
      '/discount_requests/approve': (_) => approveReply ?? const Reply(true),
      '/discount_requests/reject': (_) => const Reply(true),
      '/lpos/get_all_pending': (_) => Reply(lpos),
      '/grns/get_all_pending': (_) => const Reply(<Object>[]),
      '/lpos/approve': (_) => const Reply(true),
      '/parkings/get_parking_bill_receivables': (_) => const Reply([
            {'id': '5', 'startedAt': '2025-02-08T00:00', 'endedAt': '2025-02-10T00:00', 'qty': '1.0', 'price': '5000.0', 'discount': '0.0', 'discountStatus': null, 'amount': '5000.0', 'parkingId': '22', 'payStatus': 'PAID'},
            {'id': '75896', 'startedAt': '2025-02-11T00:00', 'endedAt': '2026-10-06T00:00', 'qty': '602.0', 'price': '5000.0', 'discount': '0.0', 'discountStatus': 'Requested', 'amount': '3010000.0', 'parkingId': '22', 'payStatus': 'UNPAID'},
          ]),
      '/parkings/get': (_) => const Reply({
            'id': '22',
            'ownerPhoneNo': '0684342214',
            'agentName': 'GRAND MOVERS',
            'agentPhoneNo': '',
            'parkingZoneName': 'TAMBAZA',
            'vehicleEquipmentCategory': 'IN-TRANSIT',
            'tformNumber': 'TZDL20250070119',
            'billingType': 'DAILY',
            'billingAmount': '5000.0',
            'billingStartAt': '2025-02-08T00:00',
          }),
    };

Future<FakeServer> _pump(
  WidgetTester tester, {
  List<String> privileges = const ['FINCDISC-ACCESS', 'PRCMT-ACCESS'],
  Reply? approveReply,
  ValueChanged<int>? onCount,
  Map<String, Reply Function(SeenRequest)> extraRoutes = const {},
}) async {
  tester.view.physicalSize = const Size(412, 2000);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  SharedPreferences.setMockInitialValues({});
  FlutterSecureStorage.setMockInitialValues({});
  await AccountStore().save([
    Account(
      id: 'a',
      host: 'http://test.local:8080',
      username: 'mgr',
      branchName: 'SSHL-HQ',
      accessToken: fakeToken(privileges: privileges),
    ),
  ], 'a');
  final server = FakeServer({
    ..._routes(approveReply: approveReply),
    ...extraRoutes,
  });
  final session = await Session.create(httpAdapter: server);
  await tester.pumpWidget(AppScope(
    session: session,
    child: MaterialApp(
      theme: buildHqTheme(),
      home: ApprovalsScreen(onCount: onCount),
    ),
  ));
  await tester.pumpAndSettle();
  return server;
}

void main() {
  group('models', () {
    test('only pending requests are kept', () {
      final item = DiscountItem.fromJson(DiscountKind.parking, _parking);
      final rows = _requests
          .map((j) => PendingDiscount.fromJson(item, j))
          .whereType<PendingDiscount>()
          .toList();
      expect(rows.map((r) => r.id), [1901]);
      expect(rows.single.afterDiscount, 2510000);
      expect(rows.single.percent.toStringAsFixed(1), '16.6');
    });

    test('a parking item is named by type, owner and chassis', () {
      final item = DiscountItem.fromJson(DiscountKind.parking, _parking);
      expect(item.title, 'NISSAN CARAVAN');
      expect(item.owner, 'DULLAH');
      expect(item.detail, 'Chassis VPE25-035926 · Card 660');
    });

    test('the agent stands in when there is no owner', () {
      final item = DiscountItem.fromJson(DiscountKind.parking, {
        ..._parking,
        'ownerFirstName': '',
      });
      expect(item.owner, 'GRAND MOVERS');
    });

    test('an LPO line without an amount is priced from quantity', () {
      final lpo = PendingOrder.fromJson(OrderKind.lpo, _lpo)!;
      expect(lpo.lines.map((l) => l.amount), [450000, 50000]);
      expect(lpo.total, 500000);
    });
  });

  testWidgets('lists the pending discount and the LPO, and counts them',
      (tester) async {
    int? count;
    final server = await _pump(tester, onCount: (n) => count = n);

    expect(find.text('PKN/22'), findsOneWidget);
    expect(find.text('NISSAN CARAVAN'), findsOneWidget);
    expect(find.text('TZS 3,010,000'), findsOneWidget);
    expect(find.text('− TZS 500,000'), findsWidgets);
    expect(find.text('TZS 2,510,000'), findsOneWidget);
    expect(find.text('Reason: Long-standing customer'), findsOneWidget);
    expect(find.text('LPO/7'), findsOneWidget);
    expect(find.text('TZS 500,000'), findsOneWidget);
    expect(count, 2);

    // Bonded items need BNDDISC-ACCESS, which this login does not have.
    expect(server.seen.where((r) => r.path.startsWith('/bond_items')), isEmpty);
    // The pending endpoints want a shop_id they ignore.
    final lpoCall =
        server.seen.firstWhere((r) => r.path == '/lpos/get_all_pending');
    expect(lpoCall.query, {'shop_id': 0});
  });

  testWidgets('approving a discount sends its id and the note',
      (tester) async {
    final server = await _pump(tester);

    await tester.tap(find.text('Approve'));
    await tester.pumpAndSettle();
    expect(find.text('Approve this discount?'), findsOneWidget);
    expect(
      find.text('PKN/22: the bill drops from TZS 3,010,000 to TZS 2,510,000.'),
      findsOneWidget,
    );
    await tester.enterText(find.byType(TextField), 'Agreed with the owner');
    await tester.tap(find.widgetWithText(TextButton, 'Approve'));
    await tester.pumpAndSettle();

    final call =
        server.seen.singleWhere((r) => r.path == '/discount_requests/approve');
    expect(call.method, 'POST');
    expect(call.body, {'id': 1901, 'comments': 'Agreed with the owner'});
    expect(find.text('Discount of TZS 500,000 approved for PKN/22.'),
        findsOneWidget);
  });

  testWidgets('cancelling sends nothing', (tester) async {
    final server = await _pump(tester);
    await tester.tap(find.text('Reject'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Cancel'));
    await tester.pumpAndSettle();
    expect(server.seen.where((r) => r.path.contains('/reject')), isEmpty);
  });

  testWidgets('rejecting sends the reason', (tester) async {
    final server = await _pump(tester);
    await tester.tap(find.text('Reject'));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), 'Too large');
    await tester.tap(find.widgetWithText(TextButton, 'Reject'));
    await tester.pumpAndSettle();

    final call =
        server.seen.singleWhere((r) => r.path == '/discount_requests/reject');
    expect(call.body, {'id': 1901, 'comments': 'Too large'});
  });

  testWidgets("a refusal is shown in the server's words", (tester) async {
    await _pump(
      tester,
      approveReply: const Reply(
        'Invalid operation: Only unpaid bill can be edited',
        status: 409,
      ),
    );
    await tester.tap(find.text('Approve'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(TextButton, 'Approve'));
    await tester.pumpAndSettle();

    expect(find.text('That did not go through'), findsOneWidget);
    expect(find.text('Invalid operation: Only unpaid bill can be edited'),
        findsOneWidget);
  });

  testWidgets('an LPO opens line by line and approves by its id',
      (tester) async {
    final server = await _pump(tester);
    await tester.tap(find.text('LPO/7'));
    await tester.pumpAndSettle();

    expect(find.text('Kilimanjaro Supplies'), findsOneWidget);
    expect(find.text('Engine oil 5L'), findsOneWidget);
    expect(find.text('10 PCS × TZS 45,000'), findsOneWidget);
    expect(find.text('30 Oct 2026'), findsOneWidget);

    await tester.tap(find.text('Approve LPO'));
    await tester.pumpAndSettle();
    expect(find.text('Approve LPO LPO/7?'), findsOneWidget);
    await tester.tap(find.widgetWithText(TextButton, 'Approve'));
    await tester.pumpAndSettle();

    final call = server.seen.singleWhere((r) => r.path == '/lpos/approve');
    expect(call.method, 'POST');
    expect(call.query, {'lpo_id': 7});
    // Back on the queue, which reloads.
    expect(find.text('Approvals'), findsOneWidget);
    expect(
      server.seen.where((r) => r.path == '/lpos/get_all_pending').length,
      2,
    );
  });

  testWidgets('a login with neither privilege sees no queue', (tester) async {
    final server = await _pump(tester, privileges: ['MNGNT-ACCESS']);
    expect(find.text('You do not have access to this'), findsOneWidget);
    expect(server.seen, isEmpty);
  });

  testWidgets('procurement only: no discount section', (tester) async {
    final server = await _pump(tester, privileges: ['PRCMT-ACCESS']);
    expect(find.text('DISCOUNT REQUESTS'), findsNothing);
    expect(find.text('LPO/7'), findsOneWidget);
    expect(server.seen.where((r) => r.path.contains('discount')), isEmpty);
  });

  testWidgets('the bill: the item, every bill, the one under request',
      (tester) async {
    final server = await _pump(tester);
    await tester.tap(find.text('View bill'));
    await tester.pumpAndSettle();

    expect(find.text('Bill · PKN/22'), findsOneWidget);
    expect(find.text('0684342214'), findsOneWidget);
    expect(find.text('TAMBAZA'), findsOneWidget);
    expect(find.text('daily at TZS 5,000'), findsOneWidget);
    expect(find.text('8 Feb 2025'), findsOneWidget);
    // Both bills, the requested one marked.
    expect(find.text('8 Feb 2025 – 10 Feb 2025'), findsOneWidget);
    expect(find.text('11 Feb 2025 – 6 Oct 2026'), findsOneWidget);
    expect(find.text('DISCOUNT REQUESTED ON THIS BILL'), findsOneWidget);
    expect(find.text('602 days × TZS 5,000'), findsOneWidget);
    expect(find.text('PAID'), findsOneWidget);
    expect(find.text('UNPAID'), findsOneWidget);
    // Paid and owing.
    expect(find.text('TZS 3,015,000'), findsOneWidget);
    expect(find.text('TZS 3,010,000'), findsWidgets);

    final bills = server.seen
        .singleWhere((r) => r.path == '/parkings/get_parking_bill_receivables');
    expect(bills.query, {'parking_id': '22'});
  });

  testWidgets('deciding from the bill goes back to the queue', (tester) async {
    final server = await _pump(tester);
    await tester.tap(find.text('View bill'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Approve'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(TextButton, 'Approve'));
    await tester.pumpAndSettle();

    expect(
      server.seen.singleWhere((r) => r.path == '/discount_requests/approve').body,
      {'id': 1901, 'comments': ''},
    );
    expect(find.text('Bill · PKN/22'), findsNothing);
    expect(find.text('Approvals'), findsOneWidget);
  });

  testWidgets('many requests: grouped by kind, each a summary to open',
      (tester) async {
    Map<String, Object?> parking(String id, String no, String type) =>
        {..._parking, 'id': id, 'no': no, 'vehicleEquipmentTypeName': type};
    Map<String, Object?> request(String id, String bill, String discount) => {
          'id': id,
          'serviceBillName': 'Parking',
          'billAmount': bill,
          'discountAmount': discount,
          'status': 'PENDING',
          'reason': 'Reason $id',
          'description': '',
        };
    final server = await _pump(tester, extraRoutes: {
      '/parkings/get_all_with_discounts_page': (_) => Reply({
            'content': [
              parking('22', 'PKN/22', 'NISSAN CARAVAN'),
              parking('23', 'PKN/23', 'TOYOTA HIACE'),
            ],
            'totalElements': 2,
          }),
      '/storages/get_all_with_discounts_page': (_) => const Reply({
            'content': [
              {
                'id': '5000',
                'no': 'STG/5000',
                'ownerFirstName': 'KODZA',
                'goodName': 'BALO',
                'warehouseName': 'KEKO',
              },
            ],
            'totalElements': 1,
          }),
      '/discount_requests': (r) => Reply([
            switch (r.query['service_id']) {
              '22' => request('1901', '3010000.0', '500000.0'),
              '23' => request('1902', '100000.0', '20000.0'),
              _ => {
                  ...request('1950', '80000.0', '10000.0'),
                  'serviceBillName': 'Storage',
                },
            },
          ]),
    });

    // One header per kind, saying how many and how much is asked.
    expect(find.text('2 requests · TZS 520,000 asked'), findsOneWidget);
    expect(find.text('1 request · TZS 10,000 asked'), findsOneWidget);
    // Summaries, not full cards: no Approve buttons yet.
    expect(find.text('PKN/22'), findsOneWidget);
    expect(find.text('PKN/23'), findsOneWidget);
    expect(find.text('STG/5000'), findsOneWidget);
    expect(find.text('17% of TZS 3,010,000'), findsOneWidget);
    expect(find.text('Approve'), findsNothing);

    // Opening one shows its details and decisions, and only its.
    await tester.tap(find.text('PKN/23'));
    await tester.pumpAndSettle();
    expect(find.text('Reason: Reason 1902'), findsOneWidget);
    expect(find.text('Reason: Reason 1901'), findsNothing);
    expect(find.text('Approve'), findsOneWidget);

    // Opening another closes it - within a kind and across kinds.
    await tester.tap(find.text('PKN/22'));
    await tester.pumpAndSettle();
    expect(find.text('Reason: Reason 1901'), findsOneWidget);
    expect(find.text('Reason: Reason 1902'), findsNothing);
    await tester.tap(find.text('STG/5000'));
    await tester.pumpAndSettle();
    expect(find.text('Reason: Reason 1950'), findsOneWidget);
    expect(find.text('Reason: Reason 1901'), findsNothing);
    expect(find.text('Approve'), findsOneWidget);

    // Tapping the open one closes it.
    await tester.tap(find.text('STG/5000'));
    await tester.pumpAndSettle();
    expect(find.text('Approve'), findsNothing);

    // Folding a kind hides its requests.
    await tester.tap(find.text('2 requests · TZS 520,000 asked'));
    await tester.pumpAndSettle();
    expect(find.text('PKN/22'), findsNothing);
    expect(find.text('STG/5000'), findsOneWidget);
    expect(server.seen.where((r) => r.path.contains('/approve')), isEmpty);
  });
}
