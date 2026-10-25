import 'package:flutter/material.dart';

import '../../app/app_scope.dart';
import '../../app/theme.dart';
import '../../core/export/report_doc.dart';
import '../../core/period.dart';
import '../../widgets/async_view.dart';
import '../../widgets/kit.dart';
import '../../widgets/period_chips.dart';

/// What every report screen shares: its title, the period chips, loading and
/// errors, pull to refresh, and the share button for what is on screen.
class ReportFrame<T> extends StatefulWidget {
  const ReportFrame({
    super.key,
    required this.title,
    required this.load,
    required this.builder,
    required this.export,
    this.isEmpty,
    this.emptyTitle = 'Nothing in this period',
    this.now,
  });

  final String title;
  final Future<T> Function(Period period) load;
  final Widget Function(BuildContext context, T data, Period period) builder;

  /// The document the share button sends.
  final ExportDoc Function(BuildContext context, T data, Period period)
      export;
  final bool Function(T data)? isEmpty;
  final String emptyTitle;

  /// Fixed "now" for tests; the real clock otherwise.
  final DateTime Function()? now;

  @override
  State<ReportFrame<T>> createState() => _ReportFrameState<T>();
}

class _ReportFrameState<T> extends State<ReportFrame<T>> {
  PeriodKind _kind = PeriodKind.today;
  DateTimeRange? _custom;

  /// Replaced with each new period, so the view loads afresh and the share
  /// button reads the data on screen, never a previous period's.
  var _view = GlobalKey<AsyncViewState<T>>();

  DateTime _now() => widget.now?.call() ?? DateTime.now();

  Period get _period => Period.of(_kind, now: _now(), custom: _custom);

  @override
  Widget build(BuildContext context) {
    final period = _period;
    return Scaffold(
      backgroundColor: HqColors.bg,
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          shareAction(context, () {
            final data = _view.currentState?.data;
            return data == null ? null : widget.export(context, data, period);
          }),
        ],
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const SizedBox(height: 8),
          Padding(
            padding: const EdgeInsets.only(left: 20),
            child: PeriodChips(
              period: period,
              now: _now,
              onChanged: (p) => setState(() {
                _kind = p.kind;
                _custom = p.kind == PeriodKind.custom ? p.range : null;
                _view = GlobalKey<AsyncViewState<T>>();
              }),
            ),
          ),
          const SizedBox(height: 4),
          Expanded(
            child: AsyncView<T>(
              key: _view,
              load: () => widget.load(period),
              isEmpty: widget.isEmpty,
              emptyTitle: widget.emptyTitle,
              emptyDetail: 'Nothing was recorded ${period.noun}. '
                  'Try another period.',
              builder: (context, data) => widget.builder(context, data, period),
            ),
          ),
        ],
      ),
    );
  }
}

/// The lines printed under a shared report's title: where it is from, and
/// that it covers the whole server like the web app's reports.
List<String> reportMeta(BuildContext context) {
  final a = AppScope.of(context).session.active;
  final now = DateTime.now();
  return [
    if (a != null && a.companyName.isNotEmpty) a.companyName,
    'All branches on this server',
    'Prepared ${formatDay(now)} '
        '${now.hour.toString().padLeft(2, '0')}:'
        '${now.minute.toString().padLeft(2, '0')}'
        '${a == null ? '' : ' by ${a.displayName.isNotEmpty ? a.displayName : a.username}'}',
  ];
}

/// "1 Oct – 5 Oct 2026", or one day.
String periodLabel(Period p) =>
    p.days == 1 ? formatDay(p.range.start) : formatRange(p.range);

/// "05 Oct 14:20".
String shortTime(DateTime? t) {
  if (t == null) return '';
  const m = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return '${t.day.toString().padLeft(2, '0')} ${m[t.month - 1]} '
      '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';
}
