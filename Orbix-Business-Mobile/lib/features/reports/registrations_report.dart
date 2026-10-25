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

/// Vehicles registered at the gate, and by whom — the web app's Vehicle
/// Registration report.
class RegistrationsReport extends StatelessWidget {
  const RegistrationsReport({super.key, this.now});

  final DateTime Function()? now;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    return ReportFrame<List<Registration>>(
      title: 'Vehicle registrations',
      now: now,
      load: (p) => ReportsService(session).registrations(p),
      isEmpty: (rows) => rows.isEmpty,
      emptyTitle: 'No registrations',
      builder: (context, rows, period) {
        final byWhom = totalsBy<Registration>(
            rows, (r) => r.registeredBy, (_) => 1);
        final withKeys = rows.where((r) => r.hasKeys).length;
        return CustomScrollView(
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
                        Text(
                          '${qty(rows.length)} '
                          '${rows.length == 1 ? 'vehicle' : 'vehicles'}',
                          style: HqText.hero.copyWith(fontSize: 30),
                        ),
                        const SizedBox(height: 2),
                        Text('${qty(withKeys)} handed in with keys',
                            style: HqText.tiny),
                      ],
                    ),
                  ),
                  const SizedBox(height: 22),
                  const SectionLabel(text: 'Registered by'),
                  HqCard(
                    child: RankedBars(rows: [
                      for (var i = 0; i < byWhom.length; i++)
                        RankRow(
                          byWhom[i].key,
                          byWhom[i].value,
                          '${qty(byWhom[i].value)}  '
                          '${(byWhom[i].value / rows.length * 100).round()}%',
                          status: i == 0 ? RankStatus.best : RankStatus.normal,
                        ),
                    ]),
                  ),
                  const SizedBox(height: 22),
                  const SectionLabel(text: 'Vehicles', trailing: 'NEWEST FIRST'),
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
                  itemBuilder: (context, i) {
                    final r = rows[i];
                    return ListTile(
                      dense: true,
                      title: Text(
                        r.vehicle.isEmpty ? r.chassis : r.vehicle,
                        style: const TextStyle(
                          fontSize: 14.5,
                          fontWeight: FontWeight.w600,
                          color: HqColors.ink,
                        ),
                      ),
                      subtitle: Text(
                        [
                          if (r.chassis.isNotEmpty) r.chassis,
                          shortTime(r.when),
                          if (r.registeredBy.isNotEmpty) r.registeredBy,
                        ].join(' · '),
                        style: HqText.tiny,
                      ),
                      trailing: r.hasKeys
                          ? const Icon(Icons.key_rounded,
                              size: 18, color: HqColors.ink3)
                          : null,
                    );
                  },
                ),
              ),
            ),
          ],
        );
      },
      export: (context, rows, period) => ExportDoc(
        title: 'Vehicle registrations',
        subtitle: periodLabel(period),
        meta: reportMeta(context),
        columns: const ['Registered', 'Vehicle', 'Chassis', 'By', 'Keys'],
        rows: [
          for (final r in rows)
            [
              Cell.text(shortTime(r.when)),
              Cell.text(r.vehicle),
              Cell.text(r.chassis),
              Cell.text(r.registeredBy),
              Cell.text(r.hasKeys ? 'Yes' : 'No'),
            ],
        ],
        totals: [
          DocTotal('Vehicles', Cell.number(rows.length.toDouble())),
        ],
      ),
    );
  }
}
