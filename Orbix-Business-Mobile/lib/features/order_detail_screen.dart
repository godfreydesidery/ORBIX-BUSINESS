import 'package:flutter/material.dart';

import '../app/app_scope.dart';
import '../app/format.dart';
import '../app/theme.dart';
import '../core/api/api_exception.dart';
import '../models/approvals.dart';
import '../services/approvals_service.dart';
import '../widgets/common.dart';
import '../widgets/kit.dart';
import 'approvals_screen.dart';

/// A pending LPO or GRN, line by line, with Approve. Pops `true` once it is
/// approved so the queue reloads.
class OrderDetailScreen extends StatefulWidget {
  const OrderDetailScreen({super.key, required this.order});

  final PendingOrder order;

  @override
  State<OrderDetailScreen> createState() => _OrderDetailScreenState();
}

class _OrderDetailScreenState extends State<OrderDetailScreen> {
  late PendingOrder _order = widget.order;
  bool _busy = false;
  bool _loadingLines = false;

  bool get _isLpo => _order.kind == OrderKind.lpo;

  String get _name => _isLpo ? 'LPO' : 'GRN';

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // The pending list normally carries the lines; fetch them if not.
    if (_order.lines.isEmpty && !_loadingLines) {
      _loadingLines = true;
      _fetchLines();
    }
  }

  Future<void> _fetchLines() async {
    try {
      final full = await ApprovalsService(AppScope.of(context).session)
          .order(_order.kind, _order.id);
      if (mounted && full != null) setState(() => _order = full);
    } on ApiException {
      // Shown without lines; the approval itself does not need them.
    }
  }

  Future<void> _approve() async {
    final o = _order;
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Approve $_name ${o.no}?'),
        content: Text(
          _isLpo
              ? 'The order to ${o.supplier.isEmpty ? 'the supplier' : o.supplier}'
                  ' for ${tzs(o.total)} becomes approved.'
              : 'The goods received from '
                  '${o.supplier.isEmpty ? 'the supplier' : o.supplier} are '
                  'approved and added to stock.',
          style: HqText.body,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Approve'),
          ),
        ],
      ),
    );
    if (ok != true || !mounted) return;

    setState(() => _busy = true);
    final navigator = Navigator.of(context);
    final messenger = ScaffoldMessenger.of(context);
    try {
      await ApprovalsService(AppScope.of(context).session).approveOrder(o);
      messenger.showSnackBar(
        SnackBar(content: Text('$_name ${o.no} approved.')),
      );
      navigator.pop(true);
    } on ApiException catch (e) {
      if (!mounted) return;
      setState(() => _busy = false);
      await showDecisionFailed(context, e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final o = _order;
    return Scaffold(
      backgroundColor: HqColors.bg,
      appBar: AppBar(title: Text('$_name ${o.no}')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
        children: [
          HqCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (o.supplier.isNotEmpty)
                  Text(o.supplier, style: HqText.verdict),
                const SizedBox(height: 6),
                if (o.shop.isNotEmpty) FigureRow(label: 'Shop', value: o.shop),
                if (o.date.isNotEmpty)
                  FigureRow(
                    label: _isLpo ? 'Ordered' : 'Received',
                    value: _day(o.date),
                  ),
                if (o.validUntil.isNotEmpty)
                  FigureRow(label: 'Valid until', value: _day(o.validUntil)),
                if (o.lpoNo.isNotEmpty) FigureRow(label: 'Against LPO', value: o.lpoNo),
                if (o.receivedBy.isNotEmpty)
                  FigureRow(label: 'Received by', value: o.receivedBy),
                const Divider(height: 18),
                FigureRow(label: 'Total', value: tzs(o.total), emphasise: true),
              ],
            ),
          ),
          const SizedBox(height: 22),
          SectionLabel(
            text: 'Items',
            trailing: '${o.lines.length} ${o.lines.length == 1 ? 'LINE' : 'LINES'}',
          ),
          if (o.lines.isEmpty)
            HqCard(
              child: Text(
                _loadingLines ? 'Loading items…' : 'No items on this $_name.',
                style: HqText.body,
              ),
            )
          else
            HqCard(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              child: Column(
                children: [
                  for (var i = 0; i < o.lines.length; i++) ...[
                    if (i > 0) const Divider(height: 1),
                    _LineRow(line: o.lines[i], isGrn: !_isLpo),
                  ],
                ],
              ),
            ),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 12),
          child: FilledButton(
            onPressed: _busy ? null : _approve,
            child: _busy
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.2,
                      color: Colors.white,
                    ),
                  )
                : Text('Approve $_name'),
          ),
        ),
      ),
    );
  }

  /// "2026-10-05" or "2026-10-05T14:20:11" → "5 Oct 2026"; anything else as
  /// written.
  static String _day(String s) {
    final d = DateTime.tryParse(s);
    return d == null ? s : formatDay(d);
  }
}

class _LineRow extends StatelessWidget {
  const _LineRow({required this.line, required this.isGrn});

  final OrderLine line;
  final bool isGrn;

  @override
  Widget build(BuildContext context) {
    final uom = line.uom.isEmpty ? '' : ' ${line.uom}';
    final ordered = line.orderedQty;
    final short = isGrn && ordered != null && line.qty < ordered;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Text(
                  line.product.isEmpty ? line.code : line.product,
                  style: const TextStyle(
                    fontSize: 14.5,
                    fontWeight: FontWeight.w600,
                    color: HqColors.ink,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Text(
                tzs(line.amount),
                style: const TextStyle(
                  fontSize: 14.5,
                  fontWeight: FontWeight.w700,
                  color: HqColors.ink,
                ),
              ),
            ],
          ),
          const SizedBox(height: 3),
          Text(
            isGrn
                ? 'Received ${qty(line.qty)}$uom'
                    '${ordered == null ? '' : ' of ${qty(ordered)} ordered'}'
                    ' · ${tzs(line.price)} each'
                : '${qty(line.qty)}$uom × ${tzs(line.price)}',
            style: HqText.tiny.copyWith(color: short ? HqColors.warn : null),
          ),
        ],
      ),
    );
  }
}
