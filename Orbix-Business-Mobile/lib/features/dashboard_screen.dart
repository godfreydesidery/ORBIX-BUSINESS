import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/theme.dart';
import '../core/json.dart';
import '../widgets/async_view.dart';
import '../widgets/common.dart';
import '../widgets/kit.dart';
import 'account_screen.dart';

/// The branch at a glance. Collections and totals arrive in the next step;
/// for now it shows where the manager is and the server's business day.
class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    final account = session.active;

    return Scaffold(
      backgroundColor: HqColors.bg,
      body: SafeArea(
        bottom: false,
        child: AsyncView<DateTime?>(
          // Keyed by account so switching branch reloads.
          key: ValueKey(account?.id),
          load: () async {
            final day = asMap(await session.api.get('/days/get_bussiness_date'));
            return parseBusinessDate(day['bussinessDate']);
          },
          builder: (context, date) => ListView(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
            children: [
              _BranchHeader(
                title: account?.title ?? '',
                subtitle: account?.companyName ?? '',
                canSwitch: true,
              ),
              const SizedBox(height: 22),
              const SectionLabel(text: 'Business day'),
              HqCard(
                child: Row(
                  children: [
                    const Icon(Icons.event_outlined, color: HqColors.brand),
                    const SizedBox(width: 12),
                    Text(
                      date == null ? 'Not set' : formatDay(date),
                      style: HqText.tileValue,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
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

/// The active branch, tappable to switch account.
class _BranchHeader extends StatelessWidget {
  const _BranchHeader({
    required this.title,
    required this.subtitle,
    required this.canSwitch,
  });

  final String title;
  final String subtitle;
  final bool canSwitch;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(HqRadii.sm),
      onTap: canSwitch ? () => showAccountSwitcher(context) : null,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: HqText.title),
                  if (subtitle.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(subtitle, style: HqText.label),
                  ],
                ],
              ),
            ),
            if (canSwitch)
              const Row(
                children: [
                  Text(
                    'Switch',
                    style: TextStyle(
                      color: HqColors.brand,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  Icon(Icons.unfold_more_rounded, color: HqColors.brand),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
