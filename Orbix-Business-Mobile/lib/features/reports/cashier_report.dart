import 'package:flutter/material.dart';

import '../../app/app_scope.dart';
import '../../app/format.dart';
import '../../app/theme.dart';
import '../../core/export/report_doc.dart';
import '../../models/dashboard.dart';
import '../../models/reports.dart';
import '../../services/reports_service.dart';
import '../../widgets/common.dart';
import '../../widgets/kit.dart';
import 'report_frame.dart';

/// What each cashier took, by area and by payment method — the web app's
/// Cash Collections, one card per cashier.
class CashierReport extends StatelessWidget {
  const CashierReport({super.key, this.now});

  final DateTime Function()? now;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    return ReportFrame<CollectionSummary>(
      title: 'Collections by cashier',
      now: now,
      load: (p) => ReportsService(session).collections(p),
      isEmpty: (d) => d.isEmpty,
      emptyTitle: 'No collections',
      builder: (context, data, period) => ListView(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
        children: [
          HqCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(periodLabel(period), style: HqText.label),
                const SizedBox(height: 4),
                Amount(tzs(data.total), style: HqText.hero.copyWith(fontSize: 30)),
                const SizedBox(height: 2),
                Text(
                  '${data.byCashier.length} '
                  '${data.byCashier.length == 1 ? 'cashier' : 'cashiers'} · '
                  'all branches on this server',
                  style: HqText.tiny,
                ),
              ],
            ),
          ),
          const SizedBox(height: 22),
          for (final c in data.byCashier) ...[
            _CashierCard(
              name: c.key,
              total: c.value,
              lines: data.lines
                  .where((l) => (l.cashier.isEmpty ? 'Unknown' : l.cashier) == c.key)
                  .toList(),
            ),
            const SizedBox(height: 12),
          ],
        ],
      ),
      export: (context, data, period) => ExportDoc(
        title: 'Collections by cashier',
        subtitle: periodLabel(period),
        meta: reportMeta(context),
        columns: const ['Cashier', 'Area', 'Method', 'Amount'],
        rows: [
          for (final c in data.byCashier)
            for (final l in data.lines.where(
                (l) => (l.cashier.isEmpty ? 'Unknown' : l.cashier) == c.key))
              if (l.amount != 0)
                [
                  Cell.text(c.key),
                  Cell.text(areaLabel(l.reason)),
                  Cell.text(payMethodLabel(l.payCode)),
                  Cell.money(l.amount),
                ],
        ],
        totals: [
          for (final c in data.byCashier) DocTotal(c.key, Cell.money(c.value)),
          DocTotal('Total', Cell.money(data.total)),
        ],
      ),
    );
  }
}

class _CashierCard extends StatelessWidget {
  const _CashierCard({
    required this.name,
    required this.total,
    required this.lines,
  });

  final String name;
  final double total;
  final List<CollectionLine> lines;

  @override
  Widget build(BuildContext context) {
    final byArea = totalsBy<CollectionLine>(
        lines, (l) => areaLabel(l.reason), (l) => l.amount);
    final byMethod = totalsBy<CollectionLine>(
        lines, (l) => payMethodLabel(l.payCode), (l) => l.amount);
    return HqCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(child: Text(name, style: HqText.verdict)),
              Text(tzs(total), style: HqText.tileValue),
            ],
          ),
          const SizedBox(height: 6),
          for (final a in byArea) FigureRow(label: a.key, value: tzs(a.value)),
          if (byMethod.length > 1) ...[
            const Divider(height: 14),
            Text(
              byMethod.map((m) => '${m.key} ${tzs(m.value)}').join(' · '),
              style: HqText.tiny,
            ),
          ],
        ],
      ),
    );
  }
}
