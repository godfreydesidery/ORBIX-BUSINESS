import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/format.dart';
import '../app/theme.dart';
import '../core/json.dart';
import '../core/period.dart';
import '../core/privileges.dart';
import '../models/dashboard.dart';
import '../services/dashboard_service.dart';
import '../widgets/async_view.dart';
import '../widgets/charts.dart';
import '../widgets/common.dart';
import '../widgets/kit.dart';
import '../widgets/period_chips.dart';
import 'account_screen.dart';

/// How the business is doing: what was collected, from where, how it was
/// paid and by whom, what is on site now, and the movement this year.
class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key, this.now});

  /// Fixed "now" for tests; the real clock otherwise.
  final DateTime Function()? now;

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  PeriodKind _kind = PeriodKind.today;
  DateTimeRange? _custom;

  DateTime _now() => widget.now?.call() ?? DateTime.now();

  Period get _period => Period.of(_kind, now: _now(), custom: _custom);

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    final account = session.active;
    final period = _period;
    final allowed = session.canAny(Privileges.dashboard);

    return Scaffold(
      backgroundColor: HqColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 12, 20, 0),
              child: _BranchHeader(
                title: account?.title ?? '',
                subtitle: account?.companyName ?? '',
              ),
            ),
            if (allowed) ...[
              const SizedBox(height: 12),
              Padding(
                padding: const EdgeInsets.only(left: 20),
                child: PeriodChips(
                  period: period,
                  now: _now,
                  onChanged: (p) => setState(() {
                    _kind = p.kind;
                    _custom = p.kind == PeriodKind.custom ? p.range : null;
                  }),
                ),
              ),
              const SizedBox(height: 4),
              Expanded(
                child: AsyncView<DashboardData>(
                  // A new account or period is a new load.
                  key: ValueKey(
                    '${account?.id}|${apiDate(period.range.start)}'
                    '|${apiDate(period.range.end)}|$_kind',
                  ),
                  load: () =>
                      DashboardService(session).load(period, now: _now()),
                  builder: (context, data) =>
                      _DashboardBody(data: data, period: period, now: _now()),
                ),
              ),
            ] else
              const Expanded(child: NoPermission(code: Privileges.management)),
          ],
        ),
      ),
    );
  }
}

class _DashboardBody extends StatelessWidget {
  const _DashboardBody({
    required this.data,
    required this.period,
    required this.now,
  });

  final DashboardData data;
  final Period period;
  final DateTime now;

  @override
  Widget build(BuildContext context) {
    final current = data.current;
    final yard = data.yard;
    final trends = data.trends;

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
      children: [
        _CollectionsHero(data: data, period: period, now: now),
        if (!current.isEmpty) ...[
          const SizedBox(height: 22),
          const SectionLabel(text: 'Where it came from'),
          HqCard(child: RankedBars(rows: _rank(current.byArea))),
          const SizedBox(height: 22),
          const SectionLabel(text: 'How it was paid'),
          HqCard(child: _MethodBreakdown(rows: current.byMethod)),
          const SizedBox(height: 22),
          SectionLabel(
            text: 'Cashiers',
            trailing: _count(current.byCashier.length, 'cashier'),
          ),
          HqCard(child: _CashierList(rows: current.byCashier)),
        ],
        if (yard != null && _yardTiles(yard).isNotEmpty) ...[
          const SizedBox(height: 22),
          const SectionLabel(text: 'On site now'),
          _TileGrid(tiles: _yardTiles(yard)),
        ],
        if (trends != null && _trendCards(trends).isNotEmpty) ...[
          const SizedBox(height: 22),
          SectionLabel(
            text: 'Checked in, ${trends.year}',
            trailing: 'BY MONTH',
          ),
          ..._trendCards(trends),
        ],
        if (yard == null || trends == null) ...[
          const SizedBox(height: 18),
          const Text(
            'Some figures could not be loaded. Pull down to try again.',
            style: HqText.tiny,
          ),
        ],
      ],
    );
  }

  List<Widget> _yardTiles(YardCounts y) => [
    if (y.hasParking)
      StatTile(
        label: 'Vehicles & equipment',
        value: qty(y.vehiclesInYard),
        delta:
            '${qty(y.vehiclesRegistered)} in · '
            '${qty(y.vehiclesCheckedOut)} out ${period.noun}',
      ),
    if (y.goodsInStorage > 0)
      StatTile(label: 'Goods in storage', value: qty(y.goodsInStorage)),
    if (y.bondedItems > 0)
      StatTile(label: 'Bonded items', value: qty(y.bondedItems)),
  ];

  List<Widget> _trendCards(YearTrends t) => [
    for (final (title, months) in [
      ('Vehicles & equipment', t.parking),
      ('Goods storage', t.storage),
      ('Bonded items', t.bond),
    ])
      if (months.any((m) => m.checkedIn > 0))
        Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: HqCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(child: Text(title, style: HqText.verdict)),
                    Text(
                      qty(months.fold<int>(0, (a, m) => a + m.checkedIn)),
                      style: HqText.tileValue,
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                ColumnTrend(
                  values: [for (final m in months) m.checkedIn.toDouble()],
                  labels: [for (final m in months) _monthShort[m.month - 1]],
                  height: 130,
                ),
              ],
            ),
          ),
        ),
  ];
}

/// The one number: what was collected, against the matching period before.
class _CollectionsHero extends StatelessWidget {
  const _CollectionsHero({
    required this.data,
    required this.period,
    required this.now,
  });

  final DashboardData data;
  final Period period;
  final DateTime now;

  @override
  Widget build(BuildContext context) {
    final total = data.current.total;
    final previous = data.previous?.total;
    final stillCounting = period.includesToday(now);
    // Only finished periods are compared as a change: today's figure so far
    // against all of yesterday always reads as a fall.
    final change = (!stillCounting && previous != null && previous > 0)
        ? (total - previous) / previous * 100
        : null;

    return HeroCard(
      question: period.kind == PeriodKind.custom
          ? 'How much did we collect, ${formatRange(period.range)}?'
          : 'How much did we collect ${period.noun}?',
      verdict: collectionsVerdict(
        total: total,
        previous: previous,
        change: change,
        period: period,
        stillCounting: stillCounting,
        topArea: data.current.byArea.firstOrNull?.key,
      ),
      heroValue: tzs(total),
      comparisons: [
        if (previous != null)
          ComparisonRow(
            label: period.previousLabel,
            value: tzs(previous),
            delta: change,
            onDark: true,
          ),
      ],
      asOf: 'Updated ${_time(data.loadedAt)} · all branches on this server',
      band: stillCounting ? TrustBand.provisional : TrustBand.posted,
      trustLabel: stillCounting ? 'SO FAR' : 'COMPLETE',
      accented: true,
    );
  }
}

/// The plain-words line above the hero number.
String collectionsVerdict({
  required double total,
  required double? previous,
  required double? change,
  required Period period,
  required bool stillCounting,
  required String? topArea,
}) {
  final noun = period.noun;
  if (total == 0) {
    return stillCounting
        ? 'Nothing collected yet $noun.'
        : 'Nothing was collected $noun.';
  }
  final from = topArea == null ? '' : ', mostly from ${topArea.toLowerCase()}';
  if (stillCounting) {
    return 'Collected so far $noun$from.';
  }
  if (change == null) {
    return previous == 0
        ? 'Collected $noun, up from nothing the period before.'
        : 'Collected $noun$from.';
  }
  final against = period.previousLabel.toLowerCase();
  if (change.abs() < 1) return 'Level with $against$from.';
  final dir = change > 0 ? 'Up' : 'Down';
  return '$dir ${change.abs().toStringAsFixed(0)}% on $against$from.';
}

class _MethodBreakdown extends StatelessWidget {
  const _MethodBreakdown({required this.rows});

  final List<MapEntry<String, double>> rows;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        ShareBar(parts: [for (final r in rows) SharePart(r.key, r.value)]),
        const SizedBox(height: 8),
        for (final r in rows) FigureRow(label: r.key, value: tzs(r.value)),
      ],
    );
  }
}

/// Every cashier, largest first — a short list on a normal day, and the one
/// a manager scans for a name that should not be there.
class _CashierList extends StatelessWidget {
  const _CashierList({required this.rows});

  final List<MapEntry<String, double>> rows;

  static const _shown = 8;

  @override
  Widget build(BuildContext context) {
    final rest = rows.skip(_shown).fold<double>(0, (a, r) => a + r.value);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        RankedBars(rows: _rank(rows.take(_shown).toList())),
        if (rows.length > _shown) ...[
          const SizedBox(height: 12),
          Text(
            '${rows.length - _shown} more · ${tzs(rest)}',
            style: HqText.tiny,
          ),
        ],
      ],
    );
  }
}

class _TileGrid extends StatelessWidget {
  const _TileGrid({required this.tiles});

  final List<Widget> tiles;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, c) {
        const gap = 12.0;
        final width = (c.maxWidth - gap) / 2;
        return Wrap(
          spacing: gap,
          runSpacing: gap,
          children: [for (final t in tiles) SizedBox(width: width, child: t)],
        );
      },
    );
  }
}

/// The active branch, tappable to switch account, with the server's
/// business day underneath.
class _BranchHeader extends StatelessWidget {
  const _BranchHeader({required this.title, required this.subtitle});

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    return InkWell(
      borderRadius: BorderRadius.circular(HqRadii.sm),
      onTap: () => showAccountSwitcher(context),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: HqText.title),
                  const SizedBox(height: 2),
                  _BusinessDay(
                    key: ValueKey(session.active?.id),
                    company: subtitle,
                  ),
                ],
              ),
            ),
            const Text(
              'Switch',
              style: TextStyle(
                color: HqColors.brand,
                fontWeight: FontWeight.w600,
              ),
            ),
            const Icon(Icons.unfold_more_rounded, color: HqColors.brand),
          ],
        ),
      ),
    );
  }
}

/// "Sighman Safari Haulage · Business day 25 Oct 2026". The business day is
/// the server's, which can lag the calendar until the day is closed.
class _BusinessDay extends StatefulWidget {
  const _BusinessDay({super.key, required this.company});

  final String company;

  @override
  State<_BusinessDay> createState() => _BusinessDayState();
}

class _BusinessDayState extends State<_BusinessDay> {
  DateTime? _day;
  bool _started = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_started) {
      _started = true;
      _load();
    }
  }

  Future<void> _load() async {
    try {
      final api = AppScope.of(context).session.api;
      final day = asMap(await api.get('/days/get_bussiness_date'));
      if (!mounted) return;
      setState(() => _day = parseBusinessDate(day['bussinessDate']));
    } catch (_) {
      // The line simply goes without the day.
    }
  }

  @override
  Widget build(BuildContext context) {
    final parts = [
      if (widget.company.isNotEmpty) widget.company,
      if (_day != null) 'Business day ${formatDay(_day!)}',
    ];
    return Text(parts.join(' · '), style: HqText.label);
  }
}

/// The business day as the server sends it: `"2026-10-25"`, or `[2026,10,25]`
/// when Jackson writes dates as arrays.
DateTime? parseBusinessDate(dynamic v) {
  if (v is List && v.length >= 3) {
    final y = asInt(v[0]), m = asInt(v[1]), d = asInt(v[2]);
    if (y != null && m != null && d != null) return DateTime(y, m, d);
    return null;
  }
  return asDate(v);
}

List<RankRow> _rank(List<MapEntry<String, double>> rows) {
  final total = rows.fold<double>(0, (a, r) => a + r.value);
  return [
    for (var i = 0; i < rows.length; i++)
      RankRow(
        rows[i].key,
        rows[i].value,
        total > 0
            ? '${tzs(rows[i].value)}  ${(rows[i].value / total * 100).round()}%'
            : tzs(rows[i].value),
        status: i == 0 ? RankStatus.best : RankStatus.normal,
      ),
  ];
}

String _count(int n, String noun) =>
    '$n ${n == 1 ? noun.toUpperCase() : '${noun.toUpperCase()}S'}';

String _time(DateTime t) =>
    '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

const _monthShort = [
  'J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D', //
];
