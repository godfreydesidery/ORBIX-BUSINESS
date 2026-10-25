import 'package:flutter/material.dart';

import 'app/app_scope.dart';
import 'app/brand.dart';
import 'app/theme.dart';
import 'core/session.dart';
import 'features/account_screen.dart';
import 'features/approvals_screen.dart';
import 'features/dashboard_screen.dart';
import 'features/reports_screen.dart';
import 'features/sign_in_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final session = await Session.create();
  runApp(OrbixManagerApp(session: session));
}

class OrbixManagerApp extends StatelessWidget {
  const OrbixManagerApp({super.key, required this.session});

  final Session session;

  @override
  Widget build(BuildContext context) {
    return AppScope(
      session: session,
      child: MaterialApp(
        title: Brand.appName,
        debugShowCheckedModeBanner: false,
        theme: buildHqTheme(),
        home: const _Root(),
      ),
    );
  }
}

/// Sign-in or the app, following the session. A session that ends — expired
/// overnight, or rejected by the server — lands back on sign-in for that
/// account, asking only for its password.
class _Root extends StatefulWidget {
  const _Root();

  @override
  State<_Root> createState() => _RootState();
}

class _RootState extends State<_Root> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      AppScope.of(context).session.recheck();
    }
  }

  bool _wasSignedIn = false;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    if (_wasSignedIn && !session.signedIn) {
      // Screens pushed over the tabs (a report, an approval) would otherwise
      // stay on top of the sign-in screen.
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
      });
    }
    _wasSignedIn = session.signedIn;
    if (session.signedIn) return const _Shell();
    final pending = session.needsPassword;
    return SignInScreen(
      // A new key per account, so the form never carries one account's
      // username into another's sign-in.
      key: ValueKey(pending?.id ?? 'new'),
      account: pending,
    );
  }
}

/// Four tabs: how the branch is doing, what is waiting, the reports, and who
/// is signed in.
class _Shell extends StatefulWidget {
  const _Shell();

  @override
  State<_Shell> createState() => _ShellState();
}

class _ShellState extends State<_Shell> {
  int _tab = 0;

  /// Items waiting for a decision, once the approvals tab has loaded.
  int _waiting = 0;

  @override
  Widget build(BuildContext context) {
    final pages = <Widget>[
      const DashboardScreen(),
      ApprovalsScreen(
        onCount: (n) {
          if (mounted && n != _waiting) setState(() => _waiting = n);
        },
      ),
      const ReportsScreen(),
      const AccountScreen(),
    ];
    final badge = _waiting > 99 ? '99+' : '$_waiting';

    return Scaffold(
      body: IndexedStack(index: _tab, children: pages),
      bottomNavigationBar: DecoratedBox(
        decoration: const BoxDecoration(
          color: HqColors.panel,
          border: Border(top: BorderSide(color: HqColors.line)),
        ),
        child: NavigationBar(
          selectedIndex: _tab,
          onDestinationSelected: (i) => setState(() => _tab = i),
          backgroundColor: HqColors.panel,
          surfaceTintColor: Colors.transparent,
          indicatorColor: HqColors.brandSoft,
          height: 66,
          labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
          destinations: [
            const NavigationDestination(
              icon: Icon(Icons.space_dashboard_outlined),
              selectedIcon: Icon(Icons.space_dashboard, color: HqColors.brand),
              label: 'Dashboard',
            ),
            NavigationDestination(
              icon: Badge(
                isLabelVisible: _waiting > 0,
                label: Text(badge),
                child: const Icon(Icons.fact_check_outlined),
              ),
              selectedIcon: Badge(
                isLabelVisible: _waiting > 0,
                label: Text(badge),
                child: const Icon(Icons.fact_check, color: HqColors.brand),
              ),
              label: 'Approvals',
            ),
            const NavigationDestination(
              icon: Icon(Icons.assessment_outlined),
              selectedIcon: Icon(Icons.assessment, color: HqColors.brand),
              label: 'Reports',
            ),
            const NavigationDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person, color: HqColors.brand),
              label: 'Account',
            ),
          ],
        ),
      ),
    );
  }
}
