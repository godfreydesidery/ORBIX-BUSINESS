import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/brand.dart';
import '../app/theme.dart';
import '../core/accounts.dart';
import '../core/api/api_exception.dart';
import '../core/config/server_config.dart';
import '../widgets/kit.dart';

/// Sign in to an Orbix Business server.
///
/// Three ways in:
/// * a new sign-in — on the phone's server ([ServerConfig]), which is hidden
///   behind the footer gesture;
/// * [account] given — that account's session ended, so only its password is
///   asked for, on the server it was saved with;
/// * [onClose] given — opened from "Add account" while another account is
///   signed in, so it can be dismissed.
class SignInScreen extends StatefulWidget {
  const SignInScreen({
    super.key,
    this.account,
    this.onSignedIn,
    this.onClose,
  });

  final Account? account;
  final VoidCallback? onSignedIn;
  final VoidCallback? onClose;

  @override
  State<SignInScreen> createState() => _SignInScreenState();
}

class _SignInScreenState extends State<SignInScreen> {
  final _user = TextEditingController();
  final _pass = TextEditingController();
  bool _obscure = true;
  bool _busy = false;
  String? _error;

  /// Taps on the footer, counting towards revealing the server address.
  ///
  /// The address is baked in at build time and a manager never needs to touch
  /// it, so it is not on the screen: an editable server field above the
  /// username invites someone to change it, and a phone pointed at the wrong
  /// server looks exactly like a broken app. Support still has to be able to
  /// move a client to a new address down a phone line, hence a gesture rather
  /// than no way in at all. Seven taps is the Android developer-options idiom
  /// — nobody arrives here by accident, and it is easy to describe out loud.
  static const _tapsToReveal = 7;
  int _taps = 0;
  DateTime? _firstTap;

  bool get _isReauth => widget.account != null;

  @override
  void initState() {
    super.initState();
    _user.text = widget.account?.username ?? '';
  }

  @override
  void dispose() {
    _user.dispose();
    _pass.dispose();
    super.dispose();
  }

  Future<void> _signIn() async {
    final session = AppScope.of(context).session;
    if (_user.text.trim().isEmpty || _pass.text.isEmpty) {
      setState(() => _error = 'Enter your username and password.');
      return;
    }
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await session.signIn(
        host: widget.account?.host ?? session.config.host,
        username: _user.text.trim(),
        password: _pass.text,
      );
      if (!mounted) return;
      widget.onSignedIn?.call();
    } on ApiException catch (e) {
      setState(() => _error = e.statusCode == 401
          ? 'That username or password is not right.'
          : e.message);
    } catch (_) {
      setState(() => _error = 'Could not sign in. Try again.');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  void _footerTapped() {
    if (_isReauth) return;
    final now = DateTime.now();
    // A slow, unrelated series of taps must not accumulate into the gesture.
    if (_firstTap == null ||
        now.difference(_firstTap!) > const Duration(seconds: 4)) {
      _firstTap = now;
      _taps = 0;
    }
    _taps++;
    if (_taps >= _tapsToReveal) {
      _taps = 0;
      _firstTap = null;
      setState(() {});
      _editHost();
      return;
    }
    setState(() {});
  }

  /// Counts down only once someone is plainly doing this on purpose, so a
  /// stray tap never hints that anything is there.
  String? get _tapHint {
    final left = _tapsToReveal - _taps;
    if (_taps < 4 || left <= 0) return null;
    return left == 1 ? '1 more' : '$left more';
  }

  Future<void> _editHost() async {
    final session = AppScope.of(context).session;
    final saved = await showDialog<String>(
      context: context,
      builder: (context) => _ServerDialog(current: session.config),
    );
    if (saved != null) {
      await session.setHost(saved);
      if (mounted) setState(() => _error = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    final account = widget.account;
    final others = session.accounts
        .where((a) => a.id != account?.id)
        .toList(growable: false);

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(gradient: HqSurfaces.heroGradient),
        child: SafeArea(
          child: ListView(
            padding: const EdgeInsets.fromLTRB(24, 16, 24, 28),
            children: [
              SizedBox(
                height: 40,
                child: widget.onClose == null
                    ? null
                    : Align(
                        alignment: Alignment.centerLeft,
                        child: IconButton(
                          tooltip: 'Close',
                          onPressed: _busy ? null : widget.onClose,
                          icon: const Icon(Icons.close_rounded,
                              color: Colors.white),
                        ),
                      ),
              ),
              const SizedBox(height: 8),
              const _BrandHeader(),
              const SizedBox(height: 30),
              if (!_isReauth && !session.config.hasHost)
                _ConnectCard(onConnect: _editHost)
              else
                _panel(account),
              if (!_isReauth && others.isNotEmpty && widget.onClose == null) ...[
                const SizedBox(height: 22),
                _SavedAccounts(accounts: others),
              ],
              if (_isReauth) ...[
                const SizedBox(height: 14),
                Center(
                  child: TextButton(
                    onPressed: _busy ? null : session.clearActive,
                    style: TextButton.styleFrom(
                        foregroundColor: HqOnDark.secondary),
                    child: const Text('Use a different account'),
                  ),
                ),
              ],
              const SizedBox(height: 14),
              _footer(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _panel(Account? account) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: HqColors.panel,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (account != null) ...[
            const Text('Your session has ended', style: HqText.verdict),
            const SizedBox(height: 4),
            Text(
              'Enter the password for ${account.title} to continue.',
              style: HqText.body,
            ),
            const SizedBox(height: 18),
          ] else if (widget.onClose != null) ...[
            const Text('Add an account', style: HqText.verdict),
            const SizedBox(height: 4),
            const Text(
              'Sign in with the login for another branch. '
              'You can switch between accounts at any time.',
              style: HqText.body,
            ),
            const SizedBox(height: 18),
          ],
          HqField(
            label: 'Username',
            controller: _user,
            readOnly: account != null,
            hint: 'Your Orbix username',
            prefix: Icons.person_outline,
          ),
          const SizedBox(height: 16),
          const Text(
            'Password',
            style: TextStyle(
              fontSize: 12.5,
              fontWeight: FontWeight.w700,
              color: HqColors.ink2,
            ),
          ),
          const SizedBox(height: 7),
          TextField(
            controller: _pass,
            obscureText: _obscure,
            autofocus: account != null,
            onSubmitted: (_) => _signIn(),
            style: const TextStyle(
              fontSize: 15,
              color: HqColors.ink,
              fontWeight: FontWeight.w500,
            ),
            decoration: InputDecoration(
              prefixIcon: const Icon(Icons.lock_outline,
                  size: 19, color: HqColors.ink3),
              suffixIcon: IconButton(
                icon: Icon(
                  _obscure
                      ? Icons.visibility_outlined
                      : Icons.visibility_off_outlined,
                  size: 19,
                  color: HqColors.ink3,
                ),
                onPressed: () => setState(() => _obscure = !_obscure),
              ),
            ),
          ),
          if (_error != null) ...[
            const SizedBox(height: 14),
            _ErrorBox(text: _error!),
          ],
          const SizedBox(height: 20),
          FilledButton(
            onPressed: _busy ? null : _signIn,
            child: _busy
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.2,
                      color: Colors.white,
                    ),
                  )
                : const Text('Sign in'),
          ),
        ],
      ),
    );
  }

  /// The way to the server address. Looks like a footnote, because to everyone
  /// but support that is all it is.
  Widget _footer() {
    return Center(
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: _busy ? null : _footerTapped,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
          child: Column(
            children: [
              const Text(
                'Protected by your company\'s server',
                style: TextStyle(fontSize: 11.5, color: HqOnDark.tertiary),
              ),
              if (!Brand.isOrbix) ...[
                // A customer-branded app still says what it is, so support
                // knows the product on the phone.
                const SizedBox(height: 4),
                const Text(
                  'Powered by Orbix Manager',
                  style: TextStyle(fontSize: 11, color: HqOnDark.tertiary),
                ),
              ],
              if (_tapHint != null) ...[
                const SizedBox(height: 4),
                Text(
                  _tapHint!,
                  style:
                      const TextStyle(fontSize: 11, color: HqOnDark.tertiary),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _BrandHeader extends StatelessWidget {
  const _BrandHeader();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: 72,
          height: 72,
          decoration: BoxDecoration(
            color: Colors.white.withValues(alpha: 0.14),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: HqOnDark.hairline),
          ),
          alignment: Alignment.center,
          child: const Text(
            Brand.mark,
            style: TextStyle(
              fontSize: 34,
              fontWeight: FontWeight.w800,
              color: Colors.white,
            ),
          ),
        ),
        const SizedBox(height: 18),
        const Text(
          Brand.appName,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.w700,
            color: Colors.white,
            letterSpacing: -0.5,
          ),
        ),
        const SizedBox(height: 6),
        const Text(
          Brand.tagline,
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 13.5, color: HqOnDark.secondary),
        ),
      ],
    );
  }
}

class _ErrorBox extends StatelessWidget {
  const _ErrorBox({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: HqColors.badSoft,
        borderRadius: BorderRadius.circular(HqRadii.sm),
      ),
      child: Row(
        children: [
          const Icon(Icons.error_outline_rounded, size: 18, color: HqColors.bad),
          const SizedBox(width: 9),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                fontSize: 13,
                color: HqColors.bad,
                height: 1.3,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Accounts saved on this phone, offered under a fresh sign-in so a manager
/// whose session ended on one branch can get back to another.
class _SavedAccounts extends StatelessWidget {
  const _SavedAccounts({required this.accounts});

  final List<Account> accounts;

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 4, bottom: 8),
          child: Text(
            'OR CONTINUE AS',
            style: TextStyle(
              fontSize: 11.5,
              fontWeight: FontWeight.w700,
              letterSpacing: 0.8,
              color: HqOnDark.secondary,
            ),
          ),
        ),
        for (final a in accounts)
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Material(
              color: Colors.white.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(14),
              child: InkWell(
                borderRadius: BorderRadius.circular(14),
                onTap: () => session.switchTo(a),
                child: Padding(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  child: Row(
                    children: [
                      const Icon(Icons.storefront_outlined,
                          color: HqOnDark.secondary, size: 20),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              a.title,
                              style: const TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.w600,
                                fontSize: 14.5,
                              ),
                            ),
                            Text(
                              a.username,
                              style: const TextStyle(
                                  color: HqOnDark.tertiary, fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                      const Icon(Icons.chevron_right_rounded,
                          color: HqOnDark.tertiary),
                    ],
                  ),
                ),
              ),
            ),
          ),
      ],
    );
  }
}

/// Edit the phone's server address. Validates the shape, and shows the current
/// one so support can read it back over the phone.
class _ServerDialog extends StatefulWidget {
  const _ServerDialog({required this.current});

  final ServerConfig current;

  @override
  State<_ServerDialog> createState() => _ServerDialogState();
}

class _ServerDialogState extends State<_ServerDialog> {
  late final _controller = TextEditingController(text: widget.current.host);
  String? _error;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _save() {
    final value = _controller.text;
    if (!ServerConfig.looksValid(value)) {
      setState(() => _error =
          'Use http://address or https://address, with an optional :port.');
      return;
    }
    Navigator.of(context).pop(ServerConfig.normalise(value));
  }

  @override
  Widget build(BuildContext context) {
    final current = widget.current;
    return AlertDialog(
      title: const Text('Server address'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            current.hasHost
                ? 'This phone is set to:'
                : 'This phone is not connected to a server yet.',
            style: HqText.tiny,
          ),
          if (current.hasHost) ...[
            const SizedBox(height: 3),
            Text(
              ServerConfig.normalise(current.host),
              style: const TextStyle(
                fontSize: 13.5,
                fontWeight: FontWeight.w700,
                color: HqColors.ink,
              ),
            ),
          ],
          const SizedBox(height: 14),
          TextField(
            controller: _controller,
            autocorrect: false,
            keyboardType: TextInputType.url,
            onSubmitted: (_) => _save(),
            decoration: InputDecoration(
              hintText: 'http://192.168.1.10:8080',
              errorText: _error,
              errorMaxLines: 3,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            'Address only — do not add ${ServerConfig.apiPath}, the app adds it.',
            style: HqText.tiny,
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: _save,
          style: FilledButton.styleFrom(minimumSize: const Size(88, 44)),
          child: const Text('Save'),
        ),
      ],
    );
  }
}

/// First launch of a build with no server baked in (the generic Orbix app):
/// the address is the one thing the app cannot guess, so it is asked for
/// openly, before the sign-in form. Once set, it goes back behind the footer
/// gesture like every other build.
class _ConnectCard extends StatelessWidget {
  const _ConnectCard({required this.onConnect});

  final VoidCallback onConnect;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: HqColors.panel,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            "Connect to your company's server",
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w700,
              color: HqColors.ink,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            'Enter the server address your administrator gave you. '
            'You only do this once.',
            style: HqText.body,
          ),
          const SizedBox(height: 20),
          FilledButton.icon(
            onPressed: onConnect,
            icon: const Icon(Icons.dns_outlined, size: 19),
            label: const Text('Enter server address'),
          ),
        ],
      ),
    );
  }
}
