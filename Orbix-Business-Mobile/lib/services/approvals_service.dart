import '../core/api/api_exception.dart';
import '../core/json.dart';
import '../core/privileges.dart';
import '../core/session.dart';
import '../models/approvals.dart';

/// What is waiting for the manager, and the decisions on it — the same
/// endpoints the web app's discount, LPO and GRN screens use.
class ApprovalsService {
  ApprovalsService(this._session);

  final Session _session;

  /// Items listed per page while gathering waiting discounts. The server
  /// lists only checked-in items with a requested discount, so one page is
  /// the normal case.
  static const _pageSize = 50;

  /// Stops a runaway list from fanning out into hundreds of calls.
  static const _maxItems = 200;

  Future<ApprovalsData> load() async {
    final kinds = DiscountKind.values
        .where((k) => _session.canAny(k.privileges))
        .toList();
    final procurement = _session.can(Privileges.procurement);
    final failed = <String>[];

    Future<T?> section<T>(String name, Future<T> Function() f) async {
      try {
        return await f();
      } on ApiException catch (e) {
        if (e.isSessionLost) rethrow;
        failed.add(name);
        return null;
      }
    }

    final results = await Future.wait<Object?>([
      kinds.isEmpty
          ? Future.value(null)
          : section('discounts', () => _discounts(kinds)),
      procurement
          ? section('LPOs', () => pendingOrders(OrderKind.lpo))
          : Future.value(null),
      procurement
          ? section('GRNs', () => pendingOrders(OrderKind.grn))
          : Future.value(null),
    ]);

    return ApprovalsData(
      discounts: kinds.isEmpty
          ? null
          : (results[0] as List<PendingDiscount>?) ?? const [],
      lpos: procurement ? (results[1] as List<PendingOrder>?) ?? const [] : null,
      grns: procurement ? (results[2] as List<PendingOrder>?) ?? const [] : null,
      failed: failed,
    );
  }

  Future<List<PendingDiscount>> _discounts(List<DiscountKind> kinds) async {
    final perKind = await Future.wait(kinds.map(_discountsFor));
    return [for (final list in perKind) ...list];
  }

  Future<List<PendingDiscount>> _discountsFor(DiscountKind kind) async {
    final items = <DiscountItem>[];
    for (var page = 0; items.length < _maxItems; page++) {
      final res = asMap(await _session.api.get(
        '/${kind.path}/get_all_with_discounts_page',
        query: {'page': page, 'size': _pageSize},
      ));
      final batch =
          asList(res['content'], (j) => DiscountItem.fromJson(kind, j));
      items.addAll(batch);
      final total = asIntOr(res['totalElements']);
      if (batch.length < _pageSize || items.length >= total) break;
    }

    final perItem = await Future.wait(items.map((item) async {
      final rows = await _session.api.get('/discount_requests', query: {
        'service_id': item.id,
        'service_name': kind.serviceName,
      });
      return asList(rows, (j) => PendingDiscount.fromJson(item, j))
          .whereType<PendingDiscount>()
          .toList();
    }));
    return [for (final list in perItem) ...list];
  }

  /// Every pending LPO or GRN on the server. The endpoint insists on a
  /// `shop_id` it then ignores.
  Future<List<PendingOrder>> pendingOrders(OrderKind kind) async {
    final rows = await _session.api.get(
      kind == OrderKind.lpo ? '/lpos/get_all_pending' : '/grns/get_all_pending',
      query: {'shop_id': 0},
    );
    return asList(rows, (j) => PendingOrder.fromJson(kind, j))
        .whereType<PendingOrder>()
        .toList();
  }

  /// The full LPO or GRN, for when the list came without its lines.
  Future<PendingOrder?> order(OrderKind kind, int id) async {
    final j = asMap(await _session.api.get(
      kind == OrderKind.lpo ? '/lpos/get' : '/grns/get',
      query: {'id': id},
    ));
    return PendingOrder.fromJson(kind, j);
  }

  /// Every bill on the item a discount is requested for. The server sends
  /// nothing at all (not an empty list) for an item without bills.
  Future<List<BillLine>> bills(DiscountItem item) async {
    final rows = await _session.api.get(
      item.kind.billsPath,
      query: {item.kind.billsParam: item.id},
    );
    return asList(rows, BillLine.fromJson);
  }

  /// What the gate recorded about the item: phone, agent, where it is, the
  /// billing rate. Shown above its bills.
  Future<List<(String, String)>> itemFacts(DiscountItem item) async {
    final j = asMap(await _session.api.get(
      '/${item.kind.path}/get',
      query: {'id': item.id},
    ));
    return discountItemFacts(item.kind, j);
  }

  Future<void> approveDiscount(int id, {String comments = ''}) =>
      _session.api.post('/discount_requests/approve',
          body: {'id': id, 'comments': comments});

  Future<void> rejectDiscount(int id, {String comments = ''}) =>
      _session.api.post('/discount_requests/reject',
          body: {'id': id, 'comments': comments});

  Future<void> approveOrder(PendingOrder order) => order.kind == OrderKind.lpo
      ? _session.api.post('/lpos/approve', query: {'lpo_id': order.id})
      : _session.api.post('/grns/approve', query: {'grn_id': order.id});
}
