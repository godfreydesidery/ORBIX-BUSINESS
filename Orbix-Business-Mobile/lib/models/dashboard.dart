import '../core/json.dart';

/// One row of `/finance_reports/get_cash_collections_by_dates`: the server
/// has already summed the amount per reason, payment method and cashier.
class CollectionLine {
  const CollectionLine({
    required this.reason,
    required this.payCode,
    required this.cashier,
    required this.amount,
  });

  factory CollectionLine.fromJson(Map<String, dynamic> j) => CollectionLine(
    reason: asStrOr(j['reason']).trim(),
    payCode: asStrOr(j['payCode']).trim(),
    cashier: asStrOr(j['cashierName']).trim(),
    amount: asNumOr(j['amount']),
  );

  final String reason;
  final String payCode;
  final String cashier;
  final double amount;
}

/// Collections for a period, totalled three ways.
class CollectionSummary {
  CollectionSummary(this.lines);

  final List<CollectionLine> lines;

  double get total => lines.fold(0, (a, l) => a + l.amount);

  bool get isEmpty => total == 0;

  /// Largest first, labelled for people.
  List<MapEntry<String, double>> get byArea =>
      _group((l) => areaLabel(l.reason));

  List<MapEntry<String, double>> get byMethod =>
      _group((l) => payMethodLabel(l.payCode));

  List<MapEntry<String, double>> get byCashier =>
      _group((l) => l.cashier.isEmpty ? 'Unknown' : l.cashier);

  List<MapEntry<String, double>> _group(String Function(CollectionLine) key) {
    final sums = <String, double>{};
    for (final l in lines) {
      if (l.amount == 0) continue;
      sums.update(key(l), (v) => v + l.amount, ifAbsent: () => l.amount);
    }
    return sums.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
  }
}

/// The business area a collection is for. The server records the reason as
/// the billing screen wrote it ("Vehicle and Equipment/Parking"); this is the
/// name a manager uses. An unknown reason is shown as written.
String areaLabel(String reason) => switch (reason) {
  'Vehicle and Equipment/Parking' => 'Parking',
  'Vehicle and Equipment/Service' => 'Parking services',
  'Goods Storage' => 'Storage',
  'Bond' => 'Bonded items',
  'Weigh Bridge' => 'Weighbridge',
  'V/Eq Maintenance' => 'Maintenance',
  'Machine/Vehicle Service' => 'Workshop',
  'Restaurant Sales' || 'Restaurant Service' => 'Restaurant',
  'Product Sale' => 'Shop sales',
  'General Payment' => 'General payments',
  '' => 'Other',
  _ => reason,
};

/// The payment methods the billing screens offer.
String payMethodLabel(String code) => switch (code.toUpperCase()) {
  'CASH' => 'Cash',
  'MPESA' => 'M-Pesa',
  'TIGOPESA' => 'Tigo Pesa',
  'HALOPESA' => 'HaloPesa',
  'AIRTELMONEY' => 'Airtel Money',
  'BANK' => 'Bank',
  'OTHER' || '' => 'Other',
  _ => code,
};

/// What is on site now, and the parking movement in the period.
///
/// Storage and bonded counts are "checked in now" whatever the dates — that is
/// all their totals endpoints return.
class YardCounts {
  const YardCounts({
    required this.vehiclesInYard,
    required this.vehiclesRegistered,
    required this.vehiclesCheckedOut,
    required this.parkingBillsPaid,
    required this.goodsInStorage,
    required this.bondedItems,
  });

  final int vehiclesInYard;
  final int vehiclesRegistered;
  final int vehiclesCheckedOut;
  final int parkingBillsPaid;
  final int goodsInStorage;
  final int bondedItems;

  bool get hasParking =>
      vehiclesInYard + vehiclesRegistered + vehiclesCheckedOut > 0;
}

/// One month of a yearly summary (`get_*_summary?year=`).
class MonthMovement {
  const MonthMovement(this.month, this.checkedIn, this.checkedOut);

  factory MonthMovement.fromJson(Map<String, dynamic> j) => MonthMovement(
    asIntOr(j['month']),
    asIntOr(j['checkedIn']),
    asIntOr(j['checkedOut']),
  );

  final int month;
  final int checkedIn;
  final int checkedOut;
}

/// A yearly summary filled out to every month so far — the server omits
/// months with no movement.
List<MonthMovement> monthsSoFar(List<MonthMovement> rows, int throughMonth) {
  final byMonth = {for (final r in rows) r.month: r};
  return [
    for (var m = 1; m <= throughMonth; m++)
      byMonth[m] ?? MonthMovement(m, 0, 0),
  ];
}

/// Movement through the yard this year, per area.
class YearTrends {
  const YearTrends({
    required this.year,
    required this.parking,
    required this.storage,
    required this.bond,
  });

  final int year;
  final List<MonthMovement> parking;
  final List<MonthMovement> storage;
  final List<MonthMovement> bond;
}

/// Everything the dashboard shows for one period. Collections are the
/// point of the screen; the other parts may fail on their own without
/// taking it down, and are then null.
class DashboardData {
  const DashboardData({
    required this.current,
    required this.previous,
    required this.yard,
    required this.trends,
    required this.loadedAt,
  });

  final CollectionSummary current;
  final CollectionSummary? previous;
  final YardCounts? yard;
  final YearTrends? trends;
  final DateTime loadedAt;
}
