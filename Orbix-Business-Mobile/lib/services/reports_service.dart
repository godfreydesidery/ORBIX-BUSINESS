import '../core/json.dart';
import '../core/period.dart';
import '../core/session.dart';
import '../models/dashboard.dart';
import '../models/reports.dart';

/// The report endpoints the web app's finance and management reports use.
/// Like those, they cover every branch on the server.
class ReportsService {
  ReportsService(this._session);

  final Session _session;

  Future<CollectionSummary> collections(Period period) async {
    final rows = await _session.api.post(
      '/finance_reports/get_cash_collections_by_dates',
      body: period.toJson(),
    );
    return CollectionSummary(asList(rows, CollectionLine.fromJson));
  }

  /// Every payment taken in [area], newest first.
  Future<List<CollectionRow>> areaCollections(
    CollectionArea area,
    Period period,
  ) async {
    final rows = await _session.api.post(area.path, body: period.toJson());
    final list = asList(rows, (j) => CollectionRow.fromJson(area, j));
    list.sort((a, b) {
      final x = a.when, y = b.when;
      if (x == null || y == null) return x == null ? 1 : -1;
      return y.compareTo(x);
    });
    return list;
  }

  /// Ranked by amount: the server's own order is not.
  Future<List<BestSeller>> bestSellers(Period period, {required bool shop}) async {
    final rows = await _session.api.post(
      shop
          ? '/sales_reports/get_fast_moving_products_report_by_dates'
          : '/sales_reports/get_fast_moving_dineables_report_by_dates',
      body: period.toJson(),
    );
    return asList(rows, BestSeller.fromJson)
      ..sort((a, b) => b.amount.compareTo(a.amount));
  }

  /// Newest first. An empty nickname means every receptionist.
  Future<List<Registration>> registrations(Period period) async {
    final rows = await _session.api.post(
      '/parking_reports/get_registration_report',
      body: period.toJson(),
      query: {'nickname': ''},
    );
    final list = asList(rows, Registration.fromJson);
    list.sort((a, b) {
      final x = a.when, y = b.when;
      if (x == null || y == null) return x == null ? 1 : -1;
      return y.compareTo(x);
    });
    return list;
  }
}
