import '../app/format.dart';
import '../core/json.dart';
import '../core/privileges.dart';

/// What a discount is requested on. The server names it in
/// `serviceBillName` and lists the waiting items per kind.
enum DiscountKind {
  parking('Parking', 'parkings', 'Parking', Privileges.parkingDiscounts,
      'get_parking_bill_receivables', 'parking_id'),
  storage('Storage', 'storages', 'Storage', Privileges.storageDiscounts,
      'get_storage_bill_receivables', 'storage_id'),
  bond('Bond', 'bond_items', 'Bonded item', Privileges.bondDiscounts,
      'get_bond_item_bill_receivables', 'bond_item_id');

  const DiscountKind(this.serviceName, this.path, this.label, this.privileges,
      this._billsEndpoint, this.billsParam);

  /// `service_name` on `/discount_requests`.
  final String serviceName;

  /// The resource listing items with a requested discount.
  final String path;
  final String label;
  final List<String> privileges;
  final String _billsEndpoint;

  /// The query parameter naming the item on [billsPath].
  final String billsParam;

  /// Every bill raised on one item.
  String get billsPath => '/$path/$_billsEndpoint';
}

/// One bill raised on a vehicle, consignment or bonded item.
class BillLine {
  const BillLine({
    required this.id,
    required this.from,
    required this.to,
    required this.days,
    required this.price,
    required this.discount,
    required this.amount,
    required this.paid,
    required this.discountStatus,
    required this.description,
  });

  final String id;

  /// The span billed, as the server wrote it ("2025-02-11T00:00").
  final String from;
  final String to;

  /// Days (or units) billed.
  final double days;
  final double price;
  final double discount;
  final double amount;
  final bool paid;

  /// "Requested", "Approved", "Rejected", or empty.
  final String discountStatus;
  final String description;

  factory BillLine.fromJson(Map<String, dynamic> j) {
    String s(String k) => asStrOr(j[k]).trim();
    // Storage and bond bills count days apart from quantity; parking does not.
    final days = asNum(j['noOfDays']);
    return BillLine(
      id: s('id'),
      from: s('startedAt'),
      to: s('endedAt'),
      days: (days == null || days == 0) ? asNumOr(j['qty']) : days,
      price: asNumOr(j['price']),
      discount: asNumOr(j['discount']),
      amount: asNumOr(j['amount']),
      paid: s('payStatus').toUpperCase() == 'PAID',
      discountStatus: s('discountStatus'),
      description: s('description'),
    );
  }

  /// Whether [request] is for this bill. The request names the bill only in
  /// its description ("Parking Bill 2025-02-11T00:00 2026-10-06T00:00"), so
  /// the span is matched; failing that, an unpaid bill of the same amount.
  bool isFor(PendingDiscount request) {
    if (from.isNotEmpty &&
        to.isNotEmpty &&
        request.description.contains(from) &&
        request.description.contains(to)) {
      return true;
    }
    return false;
  }
}

/// Label/value pairs worth reading before deciding, from `/<kind>/get`.
/// Placeholders the gate types for "none" ("-", "NA") are left out.
List<(String, String)> discountItemFacts(
    DiscountKind kind, Map<String, dynamic> j) {
  String s(String k) {
    final v = asStrOr(j[k]).trim();
    return (v == '-' || v.toUpperCase() == 'NA' || v == 'null') ? '' : v;
  }

  final facts = <(String, String)>[
    ('Phone', s('ownerPhoneNo')),
    ('Agent', [s('agentName'), s('agentPhoneNo')].where((v) => v.isNotEmpty).join(' · ')),
    ...switch (kind) {
      DiscountKind.parking => [
          ('Zone', s('parkingZoneName')),
          ('Category', s('vehicleEquipmentCategory')),
          ('T-form', s('tformNumber')),
          ('Billed', [
            s('billingType').toLowerCase(),
            if (asNumOr(j['billingAmount']) > 0) tzs(asNumOr(j['billingAmount'])),
          ].where((v) => v.isNotEmpty).join(' at ')),
          ('Billing since', _day(s('billingStartAt'))),
        ],
      DiscountKind.storage => [
          ('Warehouse', s('warehouseName')),
          ('Goods', [s('goodTypeName'), s('goodDescription')]
              .where((v) => v.isNotEmpty)
              .join(' · ')),
          ('Checked in', _day(s('checkedInAt'))),
        ],
      DiscountKind.bond => [
          ('Bond zone', s('bondZoneName')),
          ('Type', s('bondItemTypeName')),
          ('Checked in', _day(s('checkedInAt'))),
        ],
    },
  ];
  return facts.where((f) => f.$2.isNotEmpty).toList();
}

String _day(String s) {
  final d = DateTime.tryParse(s);
  if (d == null) return s;
  const m = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return '${d.day} ${m[d.month - 1]} ${d.year}';
}

/// Picks the bill a discount request is for, if it can be told.
BillLine? billFor(PendingDiscount request, List<BillLine> bills) {
  for (final b in bills) {
    if (b.isFor(request)) return b;
  }
  final sameAmount = bills
      .where((b) => !b.paid && b.amount == request.billAmount)
      .toList();
  return sameAmount.length == 1 ? sameAmount.single : null;
}

/// A checked-in vehicle, consignment or bonded item awaiting a discount
/// decision, as the `get_all_with_discounts_page` endpoints return it.
class DiscountItem {
  const DiscountItem({
    required this.kind,
    required this.id,
    required this.no,
    required this.title,
    required this.detail,
    required this.owner,
  });

  final DiscountKind kind;
  final String id;

  /// "PKN/22".
  final String no;

  /// What it is: "NISSAN CARAVAN", the goods name, the bonded item.
  final String title;

  /// How it is told apart: chassis number, card, description.
  final String detail;

  /// The owner, else the company, else the agent.
  final String owner;

  factory DiscountItem.fromJson(DiscountKind kind, Map<String, dynamic> j) {
    String s(String k) => asStrOr(j[k]).trim();
    final person = [s('ownerFirstName'), s('ownerMiddleName'), s('ownerLastName')]
        .where((p) => p.isNotEmpty)
        .join(' ');
    final owner = [person, s('ownerCompanyName'), s('agentName')]
        .firstWhere((p) => p.isNotEmpty, orElse: () => '');
    return switch (kind) {
      DiscountKind.parking => DiscountItem(
          kind: kind,
          id: s('id'),
          no: s('no'),
          title: s('vehicleEquipmentTypeName'),
          detail: [
            if (s('chasisNo').isNotEmpty) 'Chassis ${s('chasisNo')}',
            if (s('registrationNo').isNotEmpty) s('registrationNo'),
            if (s('cardNo').isNotEmpty) 'Card ${s('cardNo')}',
          ].join(' · '),
          owner: owner,
        ),
      DiscountKind.storage => DiscountItem(
          kind: kind,
          id: s('id'),
          no: s('no'),
          title: s('goodName').isNotEmpty ? s('goodName') : s('goodTypeName'),
          detail: [s('goodDescription'), s('warehouseName')]
              .where((p) => p.isNotEmpty)
              .join(' · '),
          owner: owner,
        ),
      DiscountKind.bond => DiscountItem(
          kind: kind,
          id: s('id'),
          no: s('no'),
          title: s('bondItemName').isNotEmpty
              ? s('bondItemName')
              : s('bondItemTypeName'),
          detail: [s('bondItemDescription'), s('bondZoneName')]
              .where((p) => p.isNotEmpty)
              .join(' · '),
          owner: owner,
        ),
    };
  }
}

/// One pending discount request, with the item it is for.
class PendingDiscount {
  const PendingDiscount({
    required this.id,
    required this.item,
    required this.billAmount,
    required this.discountAmount,
    required this.reason,
    required this.description,
  });

  final int id;
  final DiscountItem item;
  final double billAmount;
  final double discountAmount;
  final String reason;

  /// The bill the server wrote: "Parking Bill 2025-02-11T00:00 2026-10-06T00:00".
  final String description;

  double get afterDiscount => billAmount - discountAmount;

  double get percent => billAmount > 0 ? discountAmount / billAmount * 100 : 0;

  /// Null for a row that is not a pending request (the endpoint also returns
  /// requests decided in the last 48 hours).
  static PendingDiscount? fromJson(DiscountItem item, Map<String, dynamic> j) {
    if (asStrOr(j['status']).toUpperCase() != 'PENDING') return null;
    final id = asInt(j['id']);
    if (id == null) return null;
    return PendingDiscount(
      id: id,
      item: item,
      billAmount: asNumOr(j['billAmount']),
      discountAmount: asNumOr(j['discountAmount']),
      reason: asStrOr(j['reason']).trim(),
      description: asStrOr(j['description']).trim(),
    );
  }
}

/// A line on an LPO or GRN.
class OrderLine {
  const OrderLine({
    required this.product,
    required this.code,
    required this.uom,
    required this.qty,
    required this.orderedQty,
    required this.price,
    required this.amount,
  });

  final String product;
  final String code;
  final String uom;

  /// Ordered on an LPO; received on a GRN.
  final double qty;

  /// On a GRN, what the LPO asked for; null on an LPO.
  final double? orderedQty;
  final double price;
  final double amount;

  factory OrderLine.fromLpo(Map<String, dynamic> j) => _line(j, 'qty', null);

  factory OrderLine.fromGrn(Map<String, dynamic> j) =>
      _line(j, 'receivedQty', 'orderedQty');

  static OrderLine _line(
      Map<String, dynamic> j, String qtyKey, String? orderedKey) {
    final qty = asNumOr(j[qtyKey]);
    final price = asNumOr(j['costPriceVatIncl']);
    final amount = asNum(j['amount']);
    return OrderLine(
      product: asStrOr(j['productName']).trim().isNotEmpty
          ? asStrOr(j['productName']).trim()
          : asStrOr(j['productDescription']).trim(),
      code: asStrOr(j['productCode']).trim(),
      uom: asStrOr(j['baseUom']).trim(),
      qty: qty,
      orderedQty: orderedKey == null ? null : asNumOr(j[orderedKey]),
      price: price,
      // Some rows come without an amount; it is price times quantity.
      amount: (amount == null || amount == 0) ? price * qty : amount,
    );
  }
}

enum OrderKind { lpo, grn }

/// A pending LPO or GRN.
class PendingOrder {
  const PendingOrder({
    required this.kind,
    required this.id,
    required this.no,
    required this.supplier,
    required this.shop,
    required this.date,
    required this.validUntil,
    required this.lpoNo,
    required this.receivedBy,
    required this.lines,
  });

  final OrderKind kind;
  final int id;
  final String no;
  final String supplier;
  final String shop;

  /// Ordered (LPO) or received (GRN), as the server wrote it.
  final String date;

  /// LPO only — the server refuses to approve an expired one.
  final String validUntil;

  /// GRN only — the LPO it receives against.
  final String lpoNo;

  /// GRN only.
  final String receivedBy;
  final List<OrderLine> lines;

  double get total => lines.fold(0, (a, l) => a + l.amount);

  static PendingOrder? fromJson(OrderKind kind, Map<String, dynamic> j) {
    final id = asInt(j['id']);
    if (id == null) return null;
    String s(String k) => asStrOr(j[k]).trim();
    final isLpo = kind == OrderKind.lpo;
    final rows = isLpo ? j['lpoDetails'] : j['grnDetails'];
    return PendingOrder(
      kind: kind,
      id: id,
      no: s('no'),
      supplier: s('supplierName'),
      shop: s('shopName'),
      date: isLpo ? s('orderDate') : s('receivedAt'),
      validUntil: isLpo ? s('validUntilDate') : '',
      lpoNo: isLpo ? '' : s('lpoNo'),
      receivedBy: isLpo ? '' : s('receivedBy'),
      lines: asList(rows, isLpo ? OrderLine.fromLpo : OrderLine.fromGrn),
    );
  }
}

/// Everything waiting for this manager. A list is null when the manager may
/// not see it; a section that failed to load is recorded in [failed].
class ApprovalsData {
  const ApprovalsData({
    required this.discounts,
    required this.lpos,
    required this.grns,
    this.failed = const [],
  });

  final List<PendingDiscount>? discounts;
  final List<PendingOrder>? lpos;
  final List<PendingOrder>? grns;

  /// Names of sections that could not be loaded.
  final List<String> failed;

  int get count =>
      (discounts?.length ?? 0) + (lpos?.length ?? 0) + (grns?.length ?? 0);

  bool get canSeeAnything => discounts != null || lpos != null || grns != null;
}
