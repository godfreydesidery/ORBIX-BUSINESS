import 'package:flutter/material.dart';

import '../../app/app_scope.dart';
import '../../app/format.dart';
import '../../app/theme.dart';
import '../../core/export/report_doc.dart';
import '../../core/period.dart';
import '../../models/dashboard.dart';
import '../../models/reports.dart';
import '../../services/reports_service.dart';
import '../../widgets/charts.dart';
import '../../widgets/common.dart';
import 'report_frame.dart';

/// Every payment taken in one area — the web app's detailed collections
/// reports. A month of parking runs to thousands of rows, so the list builds
/// only what is on screen.
class AreaReport extends StatelessWidget {
  const AreaReport({super.key, required this.area, this.now});

  final CollectionArea area;
  final DateTime Function()? now;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    return ReportFrame<List<CollectionRow>>(
      title: '${area.label} collections',
      now: now,
      load: (p) => ReportsService(session).areaCollections(area, p),
      isEmpty: (rows) => rows.isEmpty,
      emptyTitle: 'No ${area.label.toLowerCase()} collections',
      builder: (context, rows, period) => _AreaBody(rows: rows, period: period),
      export: (context, rows, period) => ExportDoc(
        title: '${area.label} collections',
        subtitle: periodLabel(period),
        meta: reportMeta(context),
        columns: const [
          'Time', 'Item', 'Detail', 'Paid by', 'Cashier', 'Method',
          'Discount', 'Amount', //
        ],
        rows: [
          for (final r in rows)
            [
              Cell.text(shortTime(r.when)),
              Cell.text(r.title),
              Cell.text(r.detail),
              Cell.text(r.payer),
              Cell.text(r.cashier),
              Cell.text(payMethodLabel(r.payCode)),
              Cell.money(r.discount),
              Cell.money(r.amount),
            ],
        ],
        totals: [
          DocTotal('Payments', Cell.number(rows.length.toDouble())),
          DocTotal('Discounts', Cell.money(_sum(rows, (r) => r.discount))),
          DocTotal('Total', Cell.money(_sum(rows, (r) => r.amount))),
        ],
      ),
    );
  }
}

class _AreaBody extends StatelessWidget {
  const _AreaBody({required this.rows, required this.period});

  final List<CollectionRow> rows;
  final Period period;

  @override
  Widget build(BuildContext context) {
    final total = _sum(rows, (r) => r.amount);
    final discounts = _sum(rows, (r) => r.discount);
    final cashiers = totalsBy<CollectionRow>(rows, (r) => r.cashier, (r) => r.amount);

    return CustomScrollView(
      // Pull to refresh needs the list scrollable even when it is short.
      physics: const AlwaysScrollableScrollPhysics(),
      slivers: [
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 0),
          sliver: SliverList.list(
            children: [
              HqCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(periodLabel(period), style: HqText.label),
                    const SizedBox(height: 4),
                    Amount(tzs(total),
                        style: HqText.hero.copyWith(fontSize: 30)),
                    const SizedBox(height: 2),
                    Text(
                      '${qty(rows.length)} ${rows.length == 1 ? 'payment' : 'payments'}'
                      '${discounts > 0 ? ' · ${tzs(discounts)} in discounts' : ''}',
                      style: HqText.tiny,
                    ),
                  ],
                ),
              ),
              if (cashiers.length > 1) ...[
                const SizedBox(height: 22),
                const SectionLabel(text: 'By cashier'),
                HqCard(
                  child: RankedBars(rows: [
                    for (var i = 0; i < cashiers.length; i++)
                      RankRow(
                        cashiers[i].key,
                        cashiers[i].value,
                        tzs(cashiers[i].value),
                        status: i == 0 ? RankStatus.best : RankStatus.normal,
                      ),
                  ]),
                ),
              ],
              const SizedBox(height: 22),
              const SectionLabel(text: 'Payments', trailing: 'NEWEST FIRST'),
            ],
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 28),
          sliver: DecoratedSliver(
            decoration: BoxDecoration(
              color: HqColors.panel,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: HqColors.line),
            ),
            sliver: SliverList.separated(
              itemCount: rows.length,
              separatorBuilder: (_, _) =>
                  const Divider(height: 1, indent: 16, endIndent: 16),
              itemBuilder: (context, i) => _PaymentRow(row: rows[i]),
            ),
          ),
        ),
      ],
    );
  }
}

class _PaymentRow extends StatelessWidget {
  const _PaymentRow({required this.row});

  final CollectionRow row;

  @override
  Widget build(BuildContext context) {
    final r = row;
    final sub = [
      if (r.detail.isNotEmpty) r.detail,
      if (r.payer.isNotEmpty) r.payer,
    ].join(' · ');
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 11),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  r.title.isEmpty ? '—' : r.title,
                  style: const TextStyle(
                    fontSize: 14.5,
                    fontWeight: FontWeight.w600,
                    color: HqColors.ink,
                  ),
                ),
                if (sub.isNotEmpty) Text(sub, style: HqText.tiny),
                Text(
                  [
                    shortTime(r.when),
                    if (r.cashier.isNotEmpty) r.cashier,
                    if (r.payCode.isNotEmpty && r.payCode.toUpperCase() != 'CASH')
                      payMethodLabel(r.payCode),
                  ].join(' · '),
                  style: HqText.tiny,
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                tzs(r.amount),
                style: const TextStyle(
                  fontSize: 14.5,
                  fontWeight: FontWeight.w700,
                  color: HqColors.ink,
                ),
              ),
              if (r.discount > 0)
                Text(
                  '− ${tzs(r.discount)} discount',
                  style: HqText.tiny.copyWith(color: HqColors.warn),
                ),
            ],
          ),
        ],
      ),
    );
  }
}

double _sum(List<CollectionRow> rows, double Function(CollectionRow) f) =>
    rows.fold(0, (a, r) => a + f(r));
