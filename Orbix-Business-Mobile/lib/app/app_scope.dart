import 'package:flutter/widgets.dart';

import '../core/session.dart';

/// Holds the session and rebuilds dependents when it changes (sign-in,
/// account switch, sign-out, session ended).
class AppScope extends InheritedNotifier<Session> {
  const AppScope({super.key, required Session session, required super.child})
      : super(notifier: session);

  Session get session => notifier!;

  static AppScope of(BuildContext context) {
    final scope = context.dependOnInheritedWidgetOfExactType<AppScope>();
    assert(scope != null, 'No AppScope above this widget');
    return scope!;
  }
}
