import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/brand.dart';
import '../app/theme.dart';
import '../app/version.dart';
import '../core/accounts.dart';
import '../core/session.dart';
import '../widgets/common.dart';
import 'sign_in_screen.dart';

/// Who is signed in, the saved accounts, and the way to switch between them.
class AccountScreen extends StatelessWidget {
  const AccountScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    final active = session.active;

    return Scaffold(
      backgroundColor: HqColors.bg,
      body: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
          children: [
            if (active != null) _ProfileHeader(account: active),
            const SizedBox(height: 22),
            SectionLabel(
              text: 'Your accounts',
              trailing: session.accounts.length == 1
                  ? '1 ACCOUNT'
                  : '${session.accounts.length} ACCOUNTS',
            ),
            HqCard(
              padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
              child: Column(
                children: [
                  for (final a in session.accounts) ...[
                    if (a != session.accounts.first) const Divider(height: 1),
                    _AccountRow(account: a, showHost: _manyServers(session)),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: () => openAddAccount(context),
              icon: const Icon(Icons.add_rounded, size: 20),
              label: const Text('Add account'),
            ),
            const SizedBox(height: 6),
            const Text(
              'One account per branch. Add the login for each branch you '
              'manage, then switch here or from the dashboard.',
              style: HqText.tiny,
            ),
            const SizedBox(height: 22),
            const SectionLabel(text: 'About'),
            HqCard(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                children: [
                  _InfoRow(
                    icon: Icons.dns_outlined,
                    title: 'Server',
                    value: active?.host ?? '—',
                  ),
                  const Divider(height: 1),
                  const _InfoRow(
                    icon: Icons.info_outline_rounded,
                    title: 'Version',
                    value: kAppVersion,
                  ),
                  // Which brand build this is — support's second question,
                  // since a customer app and the generic one look different
                  // but must be diagnosed the same way.
                  const Divider(height: 1),
                  const _InfoRow(
                    icon: Icons.verified_outlined,
                    title: 'Edition',
                    value: Brand.isOrbix
                        ? Brand.appName
                        : '${Brand.appName} · powered by Orbix Manager',
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            OutlinedButton.icon(
              onPressed: () => _confirmSignOut(context, session),
              icon: const Icon(Icons.logout_rounded, size: 19),
              label: const Text('Sign out'),
              style: OutlinedButton.styleFrom(
                foregroundColor: HqColors.bad,
                side: const BorderSide(color: HqColors.line2),
              ),
            ),
          ],
        ),
      ),
    );
  }

  bool _manyServers(Session session) =>
      session.accounts.map((a) => a.host.toLowerCase()).toSet().length > 1;

  Future<void> _confirmSignOut(BuildContext context, Session session) async {
    final a = session.active;
    if (a == null) return;
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Sign out?'),
        content: Text(
          'You will sign out of ${a.title}. The account stays on this phone, '
          'so next time you only need the password.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: TextButton.styleFrom(foregroundColor: HqColors.bad),
            child: const Text('Sign out'),
          ),
        ],
      ),
    );
    if (ok == true) await session.signOut();
  }
}

/// Opens sign-in for another account on top of the current one.
Future<void> openAddAccount(BuildContext context) {
  return Navigator.of(context).push(MaterialPageRoute<void>(
    fullscreenDialog: true,
    builder: (routeContext) => SignInScreen(
      onClose: () => Navigator.of(routeContext).pop(),
      onSignedIn: () => Navigator.of(routeContext).pop(),
    ),
  ));
}

/// A bottom sheet listing the accounts, for switching from anywhere.
Future<void> showAccountSwitcher(BuildContext context) {
  final session = AppScope.of(context).session;
  return showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    builder: (sheetContext) => Container(
      decoration: const BoxDecoration(
        color: HqColors.panel,
        borderRadius: BorderRadius.vertical(top: Radius.circular(22)),
      ),
      padding: const EdgeInsets.fromLTRB(16, 18, 16, 8),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 4),
              child: Text('Switch branch', style: HqText.title),
            ),
            const SizedBox(height: 4),
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 4),
              child: Text(
                'Figures and approvals follow the account you pick.',
                style: HqText.tiny,
              ),
            ),
            const SizedBox(height: 8),
            for (final a in session.accounts)
              _AccountRow(
                account: a,
                showHost: false,
                onSwitched: () => Navigator.of(sheetContext).pop(),
                allowRemove: false,
              ),
            const Divider(height: 16),
            ListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 4),
              leading: const Icon(Icons.add_rounded, color: HqColors.brand),
              title: const Text(
                'Add account',
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  color: HqColors.brand,
                ),
              ),
              onTap: () {
                Navigator.of(sheetContext).pop();
                openAddAccount(context);
              },
            ),
          ],
        ),
      ),
    ),
  );
}

class _ProfileHeader extends StatelessWidget {
  const _ProfileHeader({required this.account});

  final Account account;

  @override
  Widget build(BuildContext context) {
    final name =
        account.displayName.isNotEmpty ? account.displayName : account.username;
    return Padding(
      padding: const EdgeInsets.only(top: 16),
      child: Row(
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: const BoxDecoration(
              gradient: HqSurfaces.brandGradient,
              shape: BoxShape.circle,
            ),
            alignment: Alignment.center,
            child: Text(
              _initials(name),
              style: const TextStyle(
                color: Colors.white,
                fontSize: 20,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name, style: HqText.title),
                const SizedBox(height: 2),
                Text(
                  [account.branchName, account.companyName]
                      .where((s) => s.isNotEmpty)
                      .join(' · '),
                  style: HqText.label,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  static String _initials(String name) {
    final parts =
        name.trim().split(RegExp(r'\s+')).where((p) => p.isNotEmpty).toList();
    if (parts.isEmpty) return '?';
    if (parts.length == 1) return parts.first[0].toUpperCase();
    return (parts.first[0] + parts.last[0]).toUpperCase();
  }
}

class _AccountRow extends StatelessWidget {
  const _AccountRow({
    required this.account,
    required this.showHost,
    this.onSwitched,
    this.allowRemove = true,
  });

  final Account account;
  final bool showHost;
  final VoidCallback? onSwitched;
  final bool allowRemove;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    final isActive = account.id == session.active?.id;
    final detail = [
      account.username,
      if (showHost) account.host,
      if (!account.isSignedIn) 'Signed out',
    ].join(' · ');

    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 12),
      leading: Icon(
        isActive ? Icons.radio_button_checked : Icons.radio_button_unchecked,
        color: isActive ? HqColors.brand : HqColors.ink3,
      ),
      title: Text(
        account.title,
        style: TextStyle(
          fontSize: 15,
          fontWeight: isActive ? FontWeight.w700 : FontWeight.w500,
          color: HqColors.ink,
        ),
      ),
      subtitle: Text(detail, style: HqText.tiny),
      trailing: allowRemove
          ? IconButton(
              tooltip: 'Remove account',
              icon: const Icon(Icons.remove_circle_outline_rounded,
                  size: 20, color: HqColors.ink3),
              onPressed: () => _confirmRemove(context, session),
            )
          : null,
      onTap: isActive
          ? onSwitched
          : () async {
              onSwitched?.call();
              await session.switchTo(account);
            },
    );
  }

  Future<void> _confirmRemove(BuildContext context, Session session) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Remove this account?'),
        content: Text(
          '${account.title} (${account.username}) will be removed from this '
          'phone. You can add it again by signing in.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: TextButton.styleFrom(foregroundColor: HqColors.bad),
            child: const Text('Remove'),
          ),
        ],
      ),
    );
    if (ok == true) await session.remove(account);
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({
    required this.icon,
    required this.title,
    required this.value,
  });

  final IconData icon;
  final String title;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 14),
      child: Row(
        children: [
          Icon(icon, size: 20, color: HqColors.ink3),
          const SizedBox(width: 12),
          Text(title, style: HqText.label),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              value,
              textAlign: TextAlign.right,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: HqColors.ink,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
