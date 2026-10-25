import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/format.dart';
import '../app/theme.dart';
import '../core/api/api_exception.dart';
import '../core/privileges.dart';
import '../models/approvals.dart';
import '../services/approvals_service.dart';
import '../widgets/async_view.dart';
import '../widgets/common.dart';
import '../widgets/kit.dart';
import 'discount_bill_screen.dart';
import 'order_detail_screen.dart';

/// Everything waiting for the manager's decision: discount requests, LPOs and
/// GRNs. Each section appears only to a login the web app would show it to.
class ApprovalsScreen extends StatefulWidget {
  const ApprovalsScreen({super.key, this.onCount});

  /// Told how many items are waiting, for the tab badge.
  final ValueChanged<int>? onCount;

  @override
  State<ApprovalsScreen> createState() => ApprovalsScreenState();
}

class ApprovalsScreenState extends State<ApprovalsScreen> {
  var _view = GlobalKey<AsyncViewState<ApprovalsData>>();
  String? _accountId;

  /// Fetches the queue again, keeping the current one on screen meanwhile.
  /// Called whenever the tab is opened or the app comes back, and after
  /// every decision.
  void refresh() => _view.currentState?.refresh();

  @override
  Widget build(BuildContext context) {
    final session = AppScope.of(context).session;
    // A different account is a different queue: a new key loads it afresh
    // (a GlobalKey would otherwise carry the old state across).
    if (session.active?.id != _accountId) {
      _accountId = session.active?.id;
      _view = GlobalKey<AsyncViewState<ApprovalsData>>();
    }
    Future<ApprovalsData> load() async {
      final data = await ApprovalsService(session).load();
      widget.onCount?.call(data.count);
      return data;
    }

    return Scaffold(
      backgroundColor: HqColors.bg,
      body: SafeArea(
        bottom: false,
        child: AsyncView<ApprovalsData>(
          key: _view,
          load: load,
          builder: (context, data) =>
              _ApprovalsBody(data: data, onChanged: refresh),
        ),
      ),
    );
  }
}

class _ApprovalsBody extends StatelessWidget {
  const _ApprovalsBody({required this.data, required this.onChanged});

  final ApprovalsData data;
  final VoidCallback onChanged;

  @override
  Widget build(BuildContext context) {
    if (!data.canSeeAnything) {
      return const NoPermission(code: Privileges.procurement);
    }
    final discounts = data.discounts;
    final lpos = data.lpos;
    final grns = data.grns;

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
      children: [
        const Text('Approvals', style: HqText.title),
        const SizedBox(height: 2),
        Text(
          data.count == 0
              ? 'Nothing is waiting for you.'
              : '${data.count} waiting for your decision · all branches on '
                  'this server',
          style: HqText.label,
        ),
        if (data.failed.isNotEmpty) ...[
          const SizedBox(height: 12),
          _Notice(
            'Could not load ${data.failed.join(' and ')}. '
            'Pull down to try again.',
          ),
        ],
        if (discounts != null) ...[
          const SizedBox(height: 22),
          SectionLabel(
            text: 'Discount requests',
            trailing: _waiting(discounts.length),
          ),
          if (discounts.isEmpty)
            const _Empty('No discount requests waiting.')
          else
            for (final d in discounts)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: _DiscountCard(discount: d, onDecided: onChanged),
              ),
        ],
        if (lpos != null) ...[
          const SizedBox(height: 22),
          SectionLabel(
            text: 'Purchase orders (LPO)',
            trailing: _waiting(lpos.length),
          ),
          _OrderList(
            orders: lpos,
            empty: 'No LPOs waiting.',
            onDecided: onChanged,
          ),
        ],
        if (grns != null) ...[
          const SizedBox(height: 22),
          SectionLabel(
            text: 'Goods received (GRN)',
            trailing: _waiting(grns.length),
          ),
          _OrderList(
            orders: grns,
            empty: 'No GRNs waiting.',
            onDecided: onChanged,
          ),
        ],
      ],
    );
  }

  static String _waiting(int n) => '$n WAITING';
}

/// One discount request, decided in place.
class _DiscountCard extends StatefulWidget {
  const _DiscountCard({required this.discount, required this.onDecided});

  final PendingDiscount discount;
  final VoidCallback onDecided;

  @override
  State<_DiscountCard> createState() => _DiscountCardState();
}

class _DiscountCardState extends State<_DiscountCard> {
  bool _busy = false;

  Future<void> _decide({required bool approve}) async {
    final decided = await decideDiscount(
      context,
      widget.discount,
      approve: approve,
      onStart: () => setState(() => _busy = true),
      onFailed: () {
        if (mounted) setState(() => _busy = false);
      },
    );
    // Decided, the buttons stay off until the queue drops the card.
    if (decided != null) widget.onDecided();
  }

  Future<void> _viewBill() async {
    final decided = await Navigator.of(context).push<bool>(MaterialPageRoute(
      builder: (_) => DiscountBillScreen(discount: widget.discount),
    ));
    if (decided == true) widget.onDecided();
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.discount;
    final item = d.item;
    return HqCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              _Tag(item.kind.label),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  item.no,
                  style: HqText.label.copyWith(fontWeight: FontWeight.w700),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          if (item.title.isNotEmpty) Text(item.title, style: HqText.verdict),
          if (item.owner.isNotEmpty) ...[
            const SizedBox(height: 2),
            Text(item.owner, style: HqText.body),
          ],
          if (item.detail.isNotEmpty) ...[
            const SizedBox(height: 2),
            Text(item.detail, style: HqText.tiny),
          ],
          const SizedBox(height: 10),
          const Divider(height: 1),
          FigureRow(label: 'Bill', value: tzs(d.billAmount)),
          FigureRow(
            label: 'Discount asked (${d.percent.toStringAsFixed(d.percent < 10 ? 1 : 0)}%)',
            value: '− ${tzs(d.discountAmount)}',
            valueColor: HqColors.warn,
          ),
          FigureRow(
            label: 'To pay after discount',
            value: tzs(d.afterDiscount),
            emphasise: true,
          ),
          if (d.reason.isNotEmpty) ...[
            const SizedBox(height: 6),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: HqColors.panel2,
                borderRadius: BorderRadius.circular(HqRadii.sm),
                border: Border.all(color: HqColors.line),
              ),
              child: Text('Reason: ${d.reason}', style: HqText.body),
            ),
          ],
          if (d.description.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(d.description, style: HqText.tiny),
          ],
          const SizedBox(height: 4),
          Align(
            alignment: Alignment.centerLeft,
            child: TextButton.icon(
              onPressed: _busy ? null : _viewBill,
              icon: const Icon(Icons.receipt_long_outlined, size: 18),
              label: const Text('View bill'),
              style: TextButton.styleFrom(
                foregroundColor: HqColors.brand,
                padding: EdgeInsets.zero,
              ),
            ),
          ),
          const SizedBox(height: 6),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: _busy ? null : () => _decide(approve: false),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: HqColors.bad,
                    minimumSize: const Size.fromHeight(46),
                  ),
                  child: const Text('Reject'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: FilledButton(
                  onPressed: _busy ? null : () => _decide(approve: true),
                  style: FilledButton.styleFrom(
                    minimumSize: const Size.fromHeight(46),
                  ),
                  child: _busy
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.2,
                            color: Colors.white,
                          ),
                        )
                      : const Text('Approve'),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Confirms a discount decision and takes an optional note. Returns the note
/// (possibly empty) to go ahead, or null to cancel.
class _DecisionDialog extends StatefulWidget {
  const _DecisionDialog({required this.discount, required this.approve});

  final PendingDiscount discount;
  final bool approve;

  @override
  State<_DecisionDialog> createState() => _DecisionDialogState();
}

class _DecisionDialogState extends State<_DecisionDialog> {
  final _note = TextEditingController();

  @override
  void dispose() {
    _note.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.discount;
    final approve = widget.approve;
    return AlertDialog(
      title: Text(approve ? 'Approve this discount?' : 'Reject this discount?'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            approve
                ? '${d.item.no}: the bill drops from ${tzs(d.billAmount)} to '
                    '${tzs(d.afterDiscount)}.'
                : '${d.item.no}: the bill stays at ${tzs(d.billAmount)}.',
            style: HqText.body,
          ),
          const SizedBox(height: 14),
          TextField(
            controller: _note,
            maxLines: 2,
            textCapitalization: TextCapitalization.sentences,
            decoration: InputDecoration(
              hintText: approve ? 'Note (optional)' : 'Why? (optional)',
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        TextButton(
          onPressed: () => Navigator.of(context).pop(_note.text.trim()),
          style: TextButton.styleFrom(
            foregroundColor: approve ? HqColors.brand : HqColors.bad,
          ),
          child: Text(approve ? 'Approve' : 'Reject'),
        ),
      ],
    );
  }
}

/// Confirms, sends and reports a decision on a discount request — from its
/// card or from its bill. Returns null when the manager cancels, true once
/// the server accepted it, false when it refused (the queue should still be
/// reloaded: the request may have changed meanwhile).
Future<bool?> decideDiscount(
  BuildContext context,
  PendingDiscount d, {
  required bool approve,
  VoidCallback? onStart,
  VoidCallback? onFailed,
}) async {
  final comments = await showDialog<String>(
    context: context,
    builder: (_) => _DecisionDialog(discount: d, approve: approve),
  );
  if (comments == null || !context.mounted) return null;

  onStart?.call();
  final service = ApprovalsService(AppScope.of(context).session);
  final messenger = ScaffoldMessenger.of(context);
  try {
    if (approve) {
      await service.approveDiscount(d.id, comments: comments);
    } else {
      await service.rejectDiscount(d.id, comments: comments);
    }
    messenger.showSnackBar(SnackBar(
      content: Text(approve
          ? 'Discount of ${tzs(d.discountAmount)} approved for ${d.item.no}.'
          : 'Discount request for ${d.item.no} rejected.'),
    ));
    return true;
  } on ApiException catch (e) {
    // The spinner stops before the explanation appears, not after.
    onFailed?.call();
    if (context.mounted) await showDecisionFailed(context, e);
    return false;
  }
}

/// Tells the manager a decision did not go through, in the server's words.
Future<void> showDecisionFailed(BuildContext context, ApiException e) {
  return showDialog<void>(
    context: context,
    builder: (context) => AlertDialog(
      title: const Text('That did not go through'),
      content: Text(e.message),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('OK'),
        ),
      ],
    ),
  );
}

class _OrderList extends StatelessWidget {
  const _OrderList({
    required this.orders,
    required this.empty,
    required this.onDecided,
  });

  final List<PendingOrder> orders;
  final String empty;
  final VoidCallback onDecided;

  @override
  Widget build(BuildContext context) {
    if (orders.isEmpty) return _Empty(empty);
    return HqCard(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        children: [
          for (var i = 0; i < orders.length; i++) ...[
            if (i > 0) const Divider(height: 1, indent: 16, endIndent: 16),
            _OrderTile(order: orders[i], onDecided: onDecided),
          ],
        ],
      ),
    );
  }
}

class _OrderTile extends StatelessWidget {
  const _OrderTile({required this.order, required this.onDecided});

  final PendingOrder order;
  final VoidCallback onDecided;

  @override
  Widget build(BuildContext context) {
    final lines = order.lines.length;
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 2),
      title: Text(
        order.no.isEmpty ? '#${order.id}' : order.no,
        style: const TextStyle(
          fontWeight: FontWeight.w700,
          color: HqColors.ink,
        ),
      ),
      subtitle: Text(
        [
          if (order.supplier.isNotEmpty) order.supplier,
          if (lines > 0) '$lines ${lines == 1 ? 'item' : 'items'}',
        ].join(' · '),
        style: HqText.tiny,
      ),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (order.total > 0)
            Text(
              tzs(order.total),
              style: const TextStyle(
                fontWeight: FontWeight.w700,
                color: HqColors.ink,
              ),
            ),
          const Icon(Icons.chevron_right_rounded, color: HqColors.ink3),
        ],
      ),
      onTap: () async {
        final decided = await Navigator.of(context).push<bool>(
          MaterialPageRoute(builder: (_) => OrderDetailScreen(order: order)),
        );
        if (decided == true) onDecided();
      },
    );
  }
}

class _Tag extends StatelessWidget {
  const _Tag(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: HqColors.brandSoft,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        text.toUpperCase(),
        style: const TextStyle(
          fontSize: 10.5,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.6,
          color: HqColors.brandD,
        ),
      ),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return HqCard(
      child: Row(
        children: [
          const Icon(Icons.check_circle_outline_rounded,
              color: HqColors.good, size: 20),
          const SizedBox(width: 10),
          Expanded(child: Text(text, style: HqText.body)),
        ],
      ),
    );
  }
}

class _Notice extends StatelessWidget {
  const _Notice(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: HqColors.warnSoft,
        borderRadius: BorderRadius.circular(HqRadii.sm),
      ),
      child: Text(text, style: HqText.body.copyWith(color: HqColors.warn)),
    );
  }
}
