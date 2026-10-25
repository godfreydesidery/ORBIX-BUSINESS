import '../core/api/api_exception.dart';
import '../core/json.dart';
import '../core/period.dart';
import '../core/session.dart';
import '../models/dashboard.dart';

/// The dashboard's figures, from the endpoints the web app's Management Board
/// and Cash Collections screens use.
///
/// None of these filter by branch — they count everything on the server, as
/// the web board does.
class DashboardService {
  DashboardService(this._session);

  final Session _session;

  Future<DashboardData> load(Period period, {DateTime? now}) async {
    final today = now ?? DateTime.now();
    // Together, so one failure never leaves another's error unhandled.
    final results = await Future.wait<Object?>([
      collections(period),
      _optional(collections(period.previous)),
      _optional(yardCounts(period)),
      _optional(yearTrends(today)),
    ]);

    return DashboardData(
      current: results[0]! as CollectionSummary,
      previous: results[1] as CollectionSummary?,
      yard: results[2] as YardCounts?,
      trends: results[3] as YearTrends?,
      loadedAt: today,
    );
  }

  Future<CollectionSummary> collections(Period period) async {
    final rows = await _session.api.post(
      '/finance_reports/get_cash_collections_by_dates',
      body: period.toJson(),
    );
    return CollectionSummary(asList(rows, CollectionLine.fromJson));
  }

  Future<YardCounts> yardCounts(Period period) async {
    final body = period.toJson();
    final results = await Future.wait([
      _session.api.post('/parking_reports/get_totals_by_dates', body: body),
      _session.api.post('/storage_reports/get_totals_by_dates', body: body),
      _session.api.post('/bond_item_reports/get_totals_by_dates', body: body),
    ]);
    final parking = asMap(results[0]);
    return YardCounts(
      vehiclesInYard: asIntOr(parking['currentTotalInYards']),
      vehiclesRegistered: asIntOr(parking['registered']),
      vehiclesCheckedOut: asIntOr(parking['checkedOut']),
      parkingBillsPaid: asIntOr(parking['paid']),
      goodsInStorage: asIntOr(asMap(results[1])['checkedIn']),
      bondedItems: asIntOr(asMap(results[2])['checkedIn']),
    );
  }

  Future<YearTrends> yearTrends(DateTime today) async {
    final query = {'year': today.year};
    final results = await Future.wait([
      _session.api.get('/parkings/get_parking_summary', query: query),
      _session.api.get('/storages/get_storage_summary', query: query),
      _session.api.get('/bond_items/get_bond_item_summary', query: query),
    ]);
    List<MonthMovement> months(dynamic rows) =>
        monthsSoFar(asList(rows, MonthMovement.fromJson), today.month);
    return YearTrends(
      year: today.year,
      parking: months(results[0]),
      storage: months(results[1]),
      bond: months(results[2]),
    );
  }

  /// A supporting figure that failed is left out rather than failing the
  /// screen — except when the session itself has ended, which must surface.
  Future<T?> _optional<T>(Future<T> f) async {
    try {
      return await f;
    } on ApiException catch (e) {
      if (e.isSessionLost) rethrow;
      return null;
    }
  }
}
