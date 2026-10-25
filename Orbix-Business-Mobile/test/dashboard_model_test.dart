// Totalling the server's collection rows, and the words above the number.
import 'package:flutter_test/flutter_test.dart';

import 'package:orbix_manager/core/period.dart';
import 'package:orbix_manager/features/dashboard_screen.dart';
import 'package:orbix_manager/models/dashboard.dart';

CollectionLine _line(
  String reason,
  String pay,
  String cashier,
  double amount,
) => CollectionLine(
  reason: reason,
  payCode: pay,
  cashier: cashier,
  amount: amount,
);

void main() {
  final summary = CollectionSummary([
    _line('Vehicle and Equipment/Parking', 'CASH', 'Asha', 300000),
    _line('Vehicle and Equipment/Parking', 'MPESA', 'Juma', 150000),
    _line('Goods Storage', 'CASH', 'Asha', 200000),
    _line('Bond', 'BANK', 'Juma', 50000),
    _line('Weigh Bridge', 'AIRTELMONEY', '', 0),
  ]);

  test('totals everything', () {
    expect(summary.total, 700000);
    expect(summary.isEmpty, isFalse);
  });

  test('groups by area, largest first, in a manager\'s words', () {
    expect(summary.byArea.map((e) => (e.key, e.value)), [
      ('Parking', 450000),
      ('Storage', 200000),
      ('Bonded items', 50000),
    ]);
  });

  test('groups by payment method and cashier, skipping zero rows', () {
    expect(summary.byMethod.map((e) => e.key), ['Cash', 'M-Pesa', 'Bank']);
    expect(summary.byCashier.map((e) => (e.key, e.value)), [
      ('Asha', 500000),
      ('Juma', 200000),
    ]);
  });

  test('reads amounts sent as numbers or strings', () {
    expect(
      CollectionLine.fromJson({'amount': '1500.5', 'reason': 'Bond'}).amount,
      1500.5,
    );
    expect(CollectionLine.fromJson({'amount': 2000}).amount, 2000);
  });

  test('labels unknown reasons and codes as the server wrote them', () {
    expect(areaLabel('Something New'), 'Something New');
    expect(payMethodLabel('CRDB'), 'CRDB');
    expect(payMethodLabel('mpesa'), 'M-Pesa');
  });

  test('fills the months the server left out', () {
    final months = monthsSoFar([
      const MonthMovement(2, 10, 4),
      const MonthMovement(4, 7, 7),
    ], 5);
    expect(months.map((m) => m.checkedIn), [0, 10, 0, 7, 0]);
  });

  group('verdict', () {
    final now = DateTime(2026, 10, 25, 14);
    final today = Period.of(PeriodKind.today, now: now);
    final yesterday = Period.of(PeriodKind.yesterday, now: now);

    test('a running period is never called a fall', () {
      expect(
        collectionsVerdict(
          total: 100,
          previous: 900,
          change: null,
          period: today,
          stillCounting: true,
          topArea: 'Parking',
        ),
        'Collected so far today, mostly from parking.',
      );
    });

    test('a finished period says which way it moved', () {
      expect(
        collectionsVerdict(
          total: 1200,
          previous: 1000,
          change: 20,
          period: yesterday,
          stillCounting: false,
          topArea: 'Storage',
        ),
        'Up 20% on the day before, mostly from storage.',
      );
      expect(
        collectionsVerdict(
          total: 900,
          previous: 1000,
          change: -10,
          period: yesterday,
          stillCounting: false,
          topArea: null,
        ),
        'Down 10% on the day before.',
      );
    });

    test('nothing collected says so', () {
      expect(
        collectionsVerdict(
          total: 0,
          previous: 500,
          change: null,
          period: today,
          stillCounting: true,
          topArea: null,
        ),
        'Nothing collected yet today.',
      );
    });
  });
}
