import 'package:flutter/material.dart';

import '../core/period.dart';
import 'kit.dart';

/// Today · Yesterday · This week · This month · Pick dates — the period
/// choice on the dashboard and every report.
class PeriodChips extends StatelessWidget {
  const PeriodChips({
    super.key,
    required this.period,
    required this.onChanged,
    this.now,
  });

  final Period period;
  final ValueChanged<Period> onChanged;

  /// Fixed "now" for tests; the real clock otherwise.
  final DateTime Function()? now;

  static const _presets = [
    (PeriodKind.today, 'Today'),
    (PeriodKind.yesterday, 'Yesterday'),
    (PeriodKind.thisWeek, 'This week'),
    (PeriodKind.thisMonth, 'This month'),
  ];

  DateTime _now() => now?.call() ?? DateTime.now();

  Future<void> _pickDates(BuildContext context) async {
    final today = _now();
    final picked = await showDateRangePicker(
      context: context,
      initialDateRange: period.range,
      firstDate: DateTime(2020),
      lastDate: DateTime(today.year, today.month, today.day),
      helpText: 'Choose the period',
      saveText: 'Apply',
    );
    if (picked != null) {
      onChanged(Period.of(PeriodKind.custom, custom: picked));
    }
  }

  @override
  Widget build(BuildContext context) {
    final custom = period.kind == PeriodKind.custom;
    final chips = [
      for (final p in _presets) p.$2,
      custom ? formatRange(period.range) : 'Pick dates',
    ];
    return FilterChipsRow(
      options: chips,
      selected: custom
          ? chips.length - 1
          : _presets.indexWhere((p) => p.$1 == period.kind),
      onSelected: (i) {
        if (i == chips.length - 1) {
          _pickDates(context);
        } else {
          onChanged(Period.of(_presets[i].$1, now: _now()));
        }
      },
    );
  }
}
