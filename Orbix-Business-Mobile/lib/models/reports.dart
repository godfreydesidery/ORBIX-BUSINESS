import '../core/json.dart';

/// A business area with its own detailed collections endpoint
/// (`/finance_reports/get_<x>_detailed_collections_by_dates`).
enum CollectionArea {
  parking('Parking', 'parking'),
  parkingServices('Parking services', 'parking_service'),
  storage('Storage', 'storage'),
  bond('Bonded items', 'bond_item'),
  weighbridge('Weighbridge', 'weigh'),
  workshop('Workshop', 'workshop'),
  maintenance('Maintenance', 'maintenance'),
  restaurant('Restaurant', 'restaurant_sales'),
  shop('Shop sales', 'sales');

  const CollectionArea(this.label, this.slug);

  final String label;
  final String slug;

  String get path => '/finance_reports/get_${slug}_detailed_collections_by_dates';
}

/// One payment taken, in any area: what it was for, from whom, by whom.
class CollectionRow {
  const CollectionRow({
    required this.when,
    required this.amount,
    required this.discount,
    required this.cashier,
    required this.payCode,
    required this.title,
    required this.detail,
    required this.payer,
  });

  /// When the money was taken.
  final DateTime? when;
  final double amount;
  final double discount;
  final String cashier;
  final String payCode;

  /// What was paid for: the vehicle, the goods, the dish.
  final String title;

  /// How it is told apart: chassis, registration, days, quantity.
  final String detail;

  /// Who paid, when the area records it.
  final String payer;

  factory CollectionRow.fromJson(CollectionArea area, Map<String, dynamic> j) {
    String s(String k) => asStrOr(j[k]).trim();
    String join(List<String> parts) =>
        parts.where((p) => p.isNotEmpty && p != '-' && p != 'NA').join(' · ');
    final days = asNum(j['days']);
    final qty = asNum(j['qty']);
    String count(double? n, String one, String many) =>
        n == null || n == 0 ? '' : '${_n(n)} ${n == 1 ? one : many}';
    final owner = join([s('ownerFirstName'), s('ownerLastName')])
        .replaceAll(' · ', ' ');

    final (title, detail, payer) = switch (area) {
      CollectionArea.parking => (
          s('vehicleEquipmentName'),
          join([s('chasisNo'), count(days, 'day', 'days')]),
          owner,
        ),
      CollectionArea.parkingServices => (
          s('serviceDescription').isNotEmpty
              ? s('serviceDescription')
              : s('vehicleEquipmentName'),
          join([s('vehicleEquipmentName'), s('chasisNo')]),
          owner,
        ),
      CollectionArea.maintenance => (
          s('issueName').isNotEmpty ? s('issueName') : s('vehicleEquipmentName'),
          join([s('vehicleEquipmentName'), s('chasisNo')]),
          owner,
        ),
      CollectionArea.storage => (
          s('goodName'),
          count(days, 'day', 'days'),
          owner,
        ),
      CollectionArea.bond => (
          s('bondItemName'),
          join([s('chasisNo'), s('bondZoneName'), count(days, 'day', 'days')]),
          owner,
        ),
      CollectionArea.weighbridge => (
          s('regNo'),
          '',
          owner,
        ),
      CollectionArea.workshop => (
          s('serviceName'),
          join([s('machineName'), s('regNo')]),
          s('ownerName') == 'NA' ? '' : s('ownerName'),
        ),
      CollectionArea.restaurant || CollectionArea.shop => (
          s('productName'),
          count(qty, 'item', 'items'),
          '',
        ),
    };

    return CollectionRow(
      when: asDate(j['dateTime']),
      amount: asNumOr(j['amount']),
      discount: asNumOr(j['discount']),
      cashier: s('cashierName'),
      payCode: s('payCode'),
      title: title,
      detail: detail,
      payer: payer,
    );
  }
}

/// A product or dish, ranked by what it brought in.
class BestSeller {
  const BestSeller(this.name, this.qty, this.amount);

  final String name;
  final double qty;
  final double amount;

  /// `get_fast_moving_products_report_by_dates` names it `productName`, the
  /// dineables report `dineableName`.
  factory BestSeller.fromJson(Map<String, dynamic> j) => BestSeller(
        asStrOr(j['productName'] ?? j['dineableName']).trim(),
        asNumOr(j['qty']),
        asNumOr(j['amount']),
      );
}

/// A vehicle registered at the gate.
class Registration {
  const Registration({
    required this.chassis,
    required this.vehicle,
    required this.registeredBy,
    required this.when,
    required this.hasKeys,
  });

  factory Registration.fromJson(Map<String, dynamic> j) => Registration(
        chassis: asStrOr(j['chassisNo']).trim(),
        vehicle: asStrOr(j['vehicleType']).trim(),
        registeredBy: asStrOr(j['registeredBy']).trim(),
        when: asDate(j['registeredDate']),
        hasKeys: asStrOr(j['keyStatus']).toUpperCase() == 'YES',
      );

  final String chassis;
  final String vehicle;
  final String registeredBy;
  final DateTime? when;
  final bool hasKeys;
}

/// Totals per key, largest first.
List<MapEntry<String, double>> totalsBy<T>(
  Iterable<T> rows,
  String Function(T) key,
  double Function(T) value,
) {
  final sums = <String, double>{};
  for (final r in rows) {
    final k = key(r).isEmpty ? 'Unknown' : key(r);
    sums.update(k, (v) => v + value(r), ifAbsent: () => value(r));
  }
  return sums.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
}

String _n(double v) =>
    v == v.roundToDouble() ? v.toStringAsFixed(0) : v.toStringAsFixed(1);
