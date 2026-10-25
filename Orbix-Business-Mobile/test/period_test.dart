// The periods on the dashboard, and what each is compared against.
//
// A comparison is only fair against the matching stretch: this month so far
// (1–25 Oct) against 1–25 Sep, not against the 25 days before it.
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:orbix_manager/core/period.dart';

// Sunday 25 October 2026.
final _now = DateTime(2026, 10, 25, 14, 30);

void _expectRange(Period p, String from, String to) {
  expect(p.toJson(), {'from': from, 'to': to});
}

void main() {
  test('today and yesterday are single days', () {
    _expectRange(
      Period.of(PeriodKind.today, now: _now),
      '2026-10-25',
      '2026-10-25',
    );
    _expectRange(
      Period.of(PeriodKind.yesterday, now: _now),
      '2026-10-24',
      '2026-10-24',
    );
  });

  test('this week starts on Monday', () {
    _expectRange(
      Period.of(PeriodKind.thisWeek, now: _now),
      '2026-10-19',
      '2026-10-25',
    );
  });

  test('this month starts on the 1st', () {
    _expectRange(
      Period.of(PeriodKind.thisMonth, now: _now),
      '2026-10-01',
      '2026-10-25',
    );
  });

  test('each is compared with the matching stretch before it', () {
    _expectRange(
      Period.of(PeriodKind.today, now: _now).previous,
      '2026-10-24',
      '2026-10-24',
    );
    _expectRange(
      Period.of(PeriodKind.thisWeek, now: _now).previous,
      '2026-10-12',
      '2026-10-18',
    );
    _expectRange(
      Period.of(PeriodKind.thisMonth, now: _now).previous,
      '2026-09-01',
      '2026-09-25',
    );
  });

  test('a month comparison clamps to the shorter month', () {
    final p = Period.of(PeriodKind.thisMonth, now: DateTime(2026, 3, 31));
    _expectRange(p.previous, '2026-02-01', '2026-02-28');
  });

  test('a custom range is compared with the same number of days before it', () {
    final p = Period.of(
      PeriodKind.custom,
      custom: DateTimeRange(
        start: DateTime(2026, 10, 10),
        end: DateTime(2026, 10, 16),
      ),
    );
    expect(p.days, 7);
    _expectRange(p.previous, '2026-10-03', '2026-10-09');
  });

  test('knows when it is still running', () {
    expect(Period.of(PeriodKind.today, now: _now).includesToday(_now), isTrue);
    expect(
      Period.of(PeriodKind.thisMonth, now: _now).includesToday(_now),
      isTrue,
    );
    expect(
      Period.of(PeriodKind.yesterday, now: _now).includesToday(_now),
      isFalse,
    );
  });

  test('crosses a year boundary', () {
    final p = Period.of(PeriodKind.yesterday, now: DateTime(2027, 1, 1));
    _expectRange(p, '2026-12-31', '2026-12-31');
  });
}
