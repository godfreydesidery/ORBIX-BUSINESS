// The monthly trend is a curve. It must draw for any year so far: January
// alone, a quiet year of zeros, or a full year — a painter that throws takes
// the whole dashboard with it.
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:orbix_manager/widgets/charts.dart';

void main() {
  final cases = <String, List<double>>{
    'January only': [42],
    'nothing yet': [0, 0, 0, 0],
    'two months': [10, 30],
    'a year with a spike': [0, 0, 1, 350, 33, 56, 56, 64, 47, 3, 0, 9],
  };
  for (final e in cases.entries) {
    testWidgets('draws: ${e.key}', (tester) async {
      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: CurveTrend(
            values: e.value,
            labels: [for (var i = 0; i < e.value.length; i++) 'M$i'],
          ),
        ),
      ));
      expect(tester.takeException(), isNull);
      expect(find.byType(CurveTrend), findsOneWidget);
    });
  }
}
