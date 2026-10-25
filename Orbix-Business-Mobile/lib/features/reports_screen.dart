import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/theme.dart';
import '../core/privileges.dart';
import '../models/reports.dart';
import '../widgets/async_view.dart';
import '../widgets/common.dart';
import 'reports/area_report.dart';
import 'reports/best_sellers_report.dart';
import 'reports/cashier_report.dart';
import 'reports/registrations_report.dart';

/// The reports a manager reads on a phone. Each opens on today, takes any
/// period, and can be sent on as PDF, spreadsheet or text.
class ReportsScreen extends StatelessWidget {
  const ReportsScreen({super.key});

  /// The areas listed under collections, in the order a haulage yard uses
  /// them. Parking services and maintenance are rare; they sit last.
  static const _areas = [
    CollectionArea.parking,
    CollectionArea.storage,
    CollectionArea.bond,
    CollectionArea.weighbridge,
    CollectionArea.workshop,
    CollectionArea.restaurant,
    CollectionArea.shop,
    CollectionArea.parkingServices,
    CollectionArea.maintenance,
  ];

  static const _areaIcons = {
    CollectionArea.parking: Icons.local_parking_rounded,
    CollectionArea.storage: Icons.warehouse_outlined,
    CollectionArea.bond: Icons.inventory_2_outlined,
    CollectionArea.weighbridge: Icons.scale_outlined,
    CollectionArea.workshop: Icons.build_outlined,
    CollectionArea.restaurant: Icons.restaurant_outlined,
    CollectionArea.shop: Icons.storefront_outlined,
    CollectionArea.parkingServices: Icons.miscellaneous_services_outlined,
    CollectionArea.maintenance: Icons.handyman_outlined,
  };

  void _open(BuildContext context, Widget report) {
    Navigator.of(context).push(MaterialPageRoute<void>(builder: (_) => report));
  }

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    if (!session.canAny(Privileges.dashboard)) {
      return const Scaffold(
        backgroundColor: HqColors.bg,
        body: SafeArea(child: NoPermission(code: Privileges.financeReports)),
      );
    }

    return Scaffold(
      backgroundColor: HqColors.bg,
      body: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
          children: [
            const Text('Reports', style: HqText.title),
            const SizedBox(height: 2),
            const Text(
              'All branches on this server. Tap the share button in a report '
              'to send it as PDF or a spreadsheet.',
              style: HqText.label,
            ),
            const SizedBox(height: 22),
            const SectionLabel(text: 'Collections'),
            _Group(children: [
              _ReportTile(
                icon: Icons.people_alt_outlined,
                title: 'By cashier',
                subtitle: 'What each cashier took, by area and method',
                onTap: () => _open(context, const CashierReport()),
              ),
              for (final a in _areas)
                _ReportTile(
                  icon: _areaIcons[a]!,
                  title: a.label,
                  subtitle: 'Every payment, with who took it',
                  onTap: () => _open(context, AreaReport(area: a)),
                ),
            ]),
            const SizedBox(height: 22),
            const SectionLabel(text: 'Sales'),
            _Group(children: [
              _ReportTile(
                icon: Icons.restaurant_menu_rounded,
                title: 'Restaurant best sellers',
                subtitle: 'Dishes by amount sold',
                onTap: () =>
                    _open(context, const BestSellersReport(shop: false)),
              ),
              _ReportTile(
                icon: Icons.shopping_bag_outlined,
                title: 'Shop best sellers',
                subtitle: 'Products by amount sold',
                onTap: () => _open(context, const BestSellersReport(shop: true)),
              ),
            ]),
            const SizedBox(height: 22),
            const SectionLabel(text: 'Yard'),
            _Group(children: [
              _ReportTile(
                icon: Icons.directions_car_outlined,
                title: 'Vehicle registrations',
                subtitle: 'Vehicles registered, and by whom',
                onTap: () => _open(context, const RegistrationsReport()),
              ),
            ]),
          ],
        ),
      ),
    );
  }
}

class _Group extends StatelessWidget {
  const _Group({required this.children});

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return HqCard(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        children: [
          for (var i = 0; i < children.length; i++) ...[
            if (i > 0) const Divider(height: 1, indent: 60),
            children[i],
          ],
        ],
      ),
    );
  }
}

class _ReportTile extends StatelessWidget {
  const _ReportTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: HqColors.brand),
      title: Text(
        title,
        style: const TextStyle(
          fontWeight: FontWeight.w600,
          color: HqColors.ink,
        ),
      ),
      subtitle: Text(subtitle, style: HqText.tiny),
      trailing: const Icon(Icons.chevron_right_rounded, color: HqColors.ink3),
      onTap: onTap,
    );
  }
}
