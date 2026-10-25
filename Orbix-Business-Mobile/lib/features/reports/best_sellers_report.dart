import 'package:flutter/material.dart';

import '../../app/app_scope.dart';
import '../../app/format.dart';
import '../../app/theme.dart';
import '../../core/export/report_doc.dart';
import '../../models/reports.dart';
import '../../services/reports_service.dart';
import '../../widgets/charts.dart';
import '../../widgets/common.dart';
import 'report_frame.dart';

/// What sold best, by amount — the web app's fast-moving reports, for the
/// restaurant's dishes or the shop's products.
class BestSellersReport extends StatelessWidget {
  const BestSellersReport({super.key, required this.shop, this.now});

  /// Shop products; otherwise restaurant dishes.
  final bool shop;
  final DateTime Function()? now;

  String get _title => shop ? 'Shop best sellers' : 'Restaurant best sellers';

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    return ReportFrame<List<BestSeller>>(
      title: _title,
      now: now,
      load: (p) => ReportsService(session).bestSellers(p, shop: shop),
      isEmpty: (rows) => rows.isEmpty,
      emptyTitle: 'No sales',
      builder: (context, rows, period) {
        final total = rows.fold<double>(0, (a, r) => a + r.amount);
        return ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
          children: [
            HqCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(periodLabel(period), style: HqText.label),
                  const SizedBox(height: 4),
                  Amount(tzs(total), style: HqText.hero.copyWith(fontSize: 30)),
                  const SizedBox(height: 2),
                  Text(
                    '${rows.length} ${shop ? 'products' : 'dishes'} sold',
                    style: HqText.tiny,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 22),
            const SectionLabel(text: 'By amount', trailing: 'QTY · AMOUNT'),
            HqCard(
              child: RankedBars(rows: [
                for (var i = 0; i < rows.length; i++)
                  RankRow(
                    rows[i].name,
                    rows[i].amount,
                    '${qty(rows[i].qty)} · ${tzs(rows[i].amount)}',
                    status: i == 0 ? RankStatus.best : RankStatus.normal,
                  ),
              ]),
            ),
          ],
        );
      },
      export: (context, rows, period) => ExportDoc(
        title: _title,
        subtitle: periodLabel(period),
        meta: reportMeta(context),
        columns: [shop ? 'Product' : 'Dish', 'Quantity', 'Amount'],
        rows: [
          for (final r in rows)
            [
              Cell.text(r.name),
              Cell.number(r.qty, decimals: r.qty == r.qty.roundToDouble() ? 0 : 2),
              Cell.money(r.amount),
            ],
        ],
        totals: [
          DocTotal(
            'Total',
            Cell.money(rows.fold<double>(0, (a, r) => a + r.amount)),
          ),
        ],
      ),
    );
  }
}
