import 'package:flutter/material.dart';

/// The periods a manager picks from, and what each is compared against.
enum PeriodKind { today, yesterday, thisWeek, thisMonth, custom }

class Period {
  const Period(this.kind, this.range);

  final PeriodKind kind;

  /// Whole days, start and end inclusive, at midnight.
  final DateTimeRange range;

  factory Period.of(PeriodKind kind, {DateTime? now, DateTimeRange? custom}) {
    final today = _day(now ?? DateTime.now());
    return switch (kind) {
      PeriodKind.today => Period(kind, DateTimeRange(start: today, end: today)),
      PeriodKind.yesterday => Period(
        kind,
        DateTimeRange(start: _addDays(today, -1), end: _addDays(today, -1)),
      ),
      PeriodKind.thisWeek => Period(
        kind,
        DateTimeRange(
          start: _addDays(today, -(today.weekday - DateTime.monday)),
          end: today,
        ),
      ),
      PeriodKind.thisMonth => Period(
        kind,
        DateTimeRange(start: DateTime(today.year, today.month), end: today),
      ),
      PeriodKind.custom => Period(
        kind,
        DateTimeRange(start: _day(custom!.start), end: _day(custom.end)),
      ),
    };
  }

  int get days => range.end.difference(range.start).inDays + 1;

  /// True while the period runs into today: its figures are still growing,
  /// so comparing them with a finished period would read as a fall.
  bool includesToday([DateTime? now]) =>
      !range.end.isBefore(_day(now ?? DateTime.now()));

  /// What this period is compared against: the matching stretch before it.
  /// "This month" so far (1–25 Oct) meets 1–25 Sep, not the 25 days before.
  Period get previous {
    switch (kind) {
      case PeriodKind.thisMonth:
        return Period(
          PeriodKind.custom,
          DateTimeRange(
            start: _shiftMonth(range.start, -1),
            end: _shiftMonth(range.end, -1),
          ),
        );
      case PeriodKind.thisWeek:
        return Period(
          PeriodKind.custom,
          DateTimeRange(
            start: _addDays(range.start, -7),
            end: _addDays(range.end, -7),
          ),
        );
      default:
        return Period(
          PeriodKind.custom,
          DateTimeRange(
            start: _addDays(range.start, -days),
            end: _addDays(range.end, -days),
          ),
        );
    }
  }

  /// "today", "yesterday", "this week" — for sentences.
  String get noun => switch (kind) {
    PeriodKind.today => 'today',
    PeriodKind.yesterday => 'yesterday',
    PeriodKind.thisWeek => 'this week',
    PeriodKind.thisMonth => 'this month',
    PeriodKind.custom => days == 1 ? 'that day' : 'in this period',
  };

  /// What [previous] is called, for the comparison row.
  String get previousLabel => switch (kind) {
    PeriodKind.today => 'Yesterday',
    PeriodKind.yesterday => 'The day before',
    PeriodKind.thisWeek => 'Same days last week',
    PeriodKind.thisMonth => 'Same days last month',
    PeriodKind.custom => 'The $days ${days == 1 ? 'day' : 'days'} before',
  };

  /// The request body the report endpoints take (`DateRange`: LocalDate
  /// `from` and `to`, both inclusive).
  Map<String, String> toJson() => {
    'from': apiDate(range.start),
    'to': apiDate(range.end),
  };

  static DateTime _day(DateTime d) => DateTime(d.year, d.month, d.day);

  // Calendar arithmetic, not Duration: a day is not always 24 hours.
  static DateTime _addDays(DateTime d, int n) =>
      DateTime(d.year, d.month, d.day + n);

  /// The same day a month away, clamped to that month's last day
  /// (31 Oct → 30 Sep).
  static DateTime _shiftMonth(DateTime d, int months) {
    final first = DateTime(d.year, d.month + months);
    final lastDay = DateTime(first.year, first.month + 1, 0).day;
    return DateTime(first.year, first.month, d.day.clamp(1, lastDay));
  }
}

/// `2026-10-25`, as Jackson reads a LocalDate.
String apiDate(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-'
    '${d.month.toString().padLeft(2, '0')}-'
    '${d.day.toString().padLeft(2, '0')}';
